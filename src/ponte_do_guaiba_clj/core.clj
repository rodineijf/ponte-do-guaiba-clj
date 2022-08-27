(ns ponte-do-guaiba-clj.core
  (:require [chime.core :as chime]
            [clojure.string :as str]
            [morse.api :as t]
            [clj-http.client :as client])
  (:import [java.time Instant Duration])
  (:gen-class))

(def token (System/getenv "TELEGRAM_BOT_TOKEN"))
(def chat-id (System/getenv "TELEGRAM_CHAT_ID"))
(def last-message-property-key "bot.last-message-sent")

(defn next-lifting-message
  []
  (-> (client/get "https://www.ccrviasul.com.br/mobile/home/BridgeHoisting")
      (:body)
      (str/replace "\"" "")
      (str/trim)))

(defn last-message-sent
  []
  (System/getProperty last-message-property-key))

(defn update-last-message-sent
  [message]
  (System/setProperty last-message-property-key message))

(defn send-message
  [message]
  (t/send-text token chat-id message))

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
  (assert (some? token) "TELEGRAM_BOT_TOKEN is missing")
  (assert (some? chat-id) "TELEGRAM_CHAT_ID is missing")
  (chime/chime-at
   (-> (chime/periodic-seq (Instant/now) (Duration/ofMinutes 10))
       rest)
   (fn [_]
     (notify-new-lifting-messages))))