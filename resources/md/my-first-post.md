# My first post

It's pretty short for now. Here's our project.clj:

    (defproject clojur.es "0.1.0-SNAPSHOT"
      :description "Sources for the clojur.es blog"
      :url "http://clojur.es"
      :license {:name "BSD 2 Clause"
                :url "http://www.opensource.org/licenses/BSD-2-Clause"}
      :dependencies [[org.clojure/clojure "1.8.0"]
                     [stasis "2.3.0"]
                     [javax.servlet/servlet-api "2.5"] ;; prevent errors on tests
                     [ring "1.5.0"]
                     [hiccup "1.0.5"]
                     [markdown-clj "0.9.68"]
                     [optimus "0.19.0"]]
      :ring {:handler clojur.web/app}
      :profiles {:dev {:plugins [[lein-ring "0.9.7"]]}})

