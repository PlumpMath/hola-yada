(defproject hola-yada "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [aleph "0.4.2-alpha4" :exclusions [org.clojure/clojure]]
                 [bidi "2.0.6" :exclusions [ring/ring-core]]
                 [ring/ring-core "1.4.0"]
                 [yada "1.1.18" :exclusions [bidi org.clojure/clojurescript buddy/buddy-sign]]])
