(defproject brutus "0.1.0-SNAPSHOT"
  :description "A simple and lightweight Entity Component System library for writing games with Clojurescript"
  :url "https://www.github.com/saebyn/brutus"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.48"]]
  :plugins [[lein-cljsbuild "1.0.6"]]

  :hooks  [leiningen.cljsbuild]
  :source-paths ["src"]
  :cljsbuild {:builds        [{:id           "dev"
                               :source-paths ["src"]
                               :compiler     {:output-to     "target/brutus.js"
                                              :output-dir    "target/js-dev"
                                              :optimizations :none
                                              :pretty-print  true
                                              :source-map    true}}]})
