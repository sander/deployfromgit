(ns deployfromgit.core
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]
            [ring.util.response :as response]
            [ring.util.request :as request]
            [clojure.string :as str])
  (:import (java.time LocalDateTime)
           (java.io File)))

(def target
  (or (System/getProperty "deployfromgit.target")
      "C:/Users/Sander/Dev/deployfromgit/testing"))
(def git-ssh-command
  (or (System/getProperty "deployfromgit.git.ssh.command")
      "\"/c/Program Files/Git/usr/bin/ssh.exe\" -i /c/Users/Sander/Dev/deployfromgit/testing/id_rsa"))

(println "started deployfromgit" [target git-ssh-command])

(def runner (agent nil))

(add-watch runner :logger #(println (str (LocalDateTime/now)) %4))
(set-error-handler! runner #(println (str (LocalDateTime/now)) "EXCEPTION" %2))

(defn pull
  [_ path]
  (let [builder (doto (ProcessBuilder. ["git" "pull"])
                  (.directory (apply io/file target path)))]
    (.put (.environment builder) "GIT_SSH_COMMAND" git-ssh-command)
    [:pulled path (-> builder .start .waitFor)]))

(defn start-pulling
  [_ path]
  (send-off runner pull path)
  [:pulling path])

(defmulti path (fn [host _] host))

(defmethod path "bitbucket.org"
  [host {{{user "username"} "owner"
          repository "name"} "repository"
         {[{{branch "name"} "new"}] "changes"} "push"}]
  [host user repository branch])

(defmethod path "github.com"
  [host {{repository "name" {user "name"} "owner"} "repository"
         ref "ref"}]
  (let [[_ branch] (re-find #"^refs/heads/([^/]+)$" (or ref ""))]
    [host user repository branch]))

(defn present?
  [path]
  (.isDirectory ^File (apply io/file target path)))

(defmulti handler (fn [{:keys [request-method] :as request}]
                    [request-method (request/path-info request)]))

(defmethod handler :default
  [_]
  (response/not-found "not found"))

(defn handle
  [host {:keys [body]}]
  (try
    (when-not body (throw (Exception. "no body")))
    (with-open [reader (io/reader body)]
      (let [path (path host (json/read reader))]
        (when-not (present? path) (throw (Exception. (str "not tracking " (str/join \/ path)))))
        (send-off runner start-pulling path)))
    (response/response "ok")
    (catch Exception ex
      (response/response (str "error: " (.getMessage ex))))))

(defmethod handler [:post "/bitbucket"]
  [req]
  (handle "bitbucket.org" req))

(defmethod handler [:post "/github"]
  [req]
  (handle "github.com" req))

#_
(defmethod handler [:get "/status"]
  [_]
  (response/response (str (or @runner "idle"))))

