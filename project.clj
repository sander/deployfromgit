(defproject deployfromgit "0.1.0"
  :description "Servlet that provides git-pull webhooks for GitHub and Bitbucket"
  :url "https://github.com/sander/deployfromgit"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [ring "1.5.0"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler deployfromgit.core/handler})
