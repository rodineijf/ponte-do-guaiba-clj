(ns ponte-do-guaiba-clj.core
  (:require [clojure.string :as str]
            [morse.api :as t]
            [clojure.java.jdbc :as jdbc]
            [clj-http.client :as client]
            [ponte-do-guaiba-clj.env :as env])
  (:gen-class))


(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     env/sqlite-path})

(defn create-db
  []
  (jdbc/db-do-commands
   db
   (jdbc/create-table-ddl :messages_sent
                          [[:id :integer "PRIMARY KEY"]
                           [:body :text]
                           [:timestamp :datetime :default :current_timestamp]]
                          {:conditional? true})))

(defn next-lifting-message
  []
  (-> (client/get "https://www.ccrviasul.com.br/mobile/home/BridgeHoisting")
      (:body)
      (str/replace "\"" "")
      (str/trim)))

(defn last-message-sent
  []
  (-> db
      (jdbc/query ["SELECT body FROM messages_sent ORDER BY id DESC LIMIT 1"])
      first
      :body))

(defn update-last-message-sent
  [message]
  (jdbc/insert! db :messages_sent {:body message}))

(defn send-message
  [message]
  (t/send-text env/telegram-token env/telegram-chat-id message))

(defn notify-new-lifting-messages
  []
  (println "=> Checking for new messages")
  (let [next-lifting-message (next-lifting-message)
        last-message-sent    (last-message-sent)]
    (if-not (= next-lifting-message last-message-sent)
      (do (send-message next-lifting-message)
          (update-last-message-sent next-lifting-message))
      (println "=> Nothing changed"))))

(defn -main
  [& args]
  (println "=> Starting")
  (assert (some? env/telegram-token) "TELEGRAM_BOT_TOKEN is missing")
  (assert (some? env/telegram-chat-id) "TELEGRAM_CHAT_ID is missing")
  (create-db)
  (notify-new-lifting-messages))