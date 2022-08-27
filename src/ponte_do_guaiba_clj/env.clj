(ns ponte-do-guaiba-clj.env
  (:require [clojure.java.io :as io]))

(def telegram-token   (System/getenv "TELEGRAM_BOT_TOKEN"))
(def telegram-chat-id (System/getenv "TELEGRAM_CHAT_ID"))
(def sqlite-path      (-> (or (System/getenv "SQLITE_DIR") "db")
                          (io/file "database.db")
                          .getAbsolutePath))