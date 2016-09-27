(ns clojur.web
  (:require [stasis.core :as stasis]
            [hiccup.page :refer [html5]]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [markdown.core :as markdown]))

(defn layout-page
  ""
  [page]
  (html5 
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1.0"}]
     [:title "Clojur.es - Clojure en español"]
     [:link {:rel "stylesheet" :href "/styles/styles.css"}]
     [:link {:rel "stylesheet" :href "/js/highlight/styles/atelier-dune-light.css"}]]
    [:body
     [:div.logo "clojur.es"]
     [:div.body page]
     [:script {:src "/js/highlight/highlight.pack.js"}]
     [:script "hljs.initHighlightingOnLoad();"]]))

(defn about-page
  [request]
  (layout-page (slurp (io/resource "partials/about.html"))))

(defn partial-pages
  "Gather all partials and add its layout"
  [pages]
  (zipmap (keys pages)
          (map layout-page (vals pages))))

(defn- md-page 
  [page]
  (println "getting pages " page)
  (layout-page (markdown/md-to-html-string page)))

;; this could be improved, can we do the layout in markdown?
(defn markdown-pages
  ""
  [pages]
  (zipmap (map #(string/replace % #"\.md$" "/") (keys pages))
          (map md-page (vals pages))))

(defn get-pages
  "Get any generated pages + static"
  []
  (stasis/merge-page-sources
    {:assets (stasis/slurp-resources "assets" #".*\.(html|css|js)$")
     :public (stasis/slurp-resources "public" #".*\.(html|css|js)$")
     :partials (partial-pages (stasis/slurp-resources "partials" #".*\.(html|css|js)$"))
     :markdown (markdown-pages (stasis/slurp-resources "md" #"\.md$"))}))

;; get anything in the public resources and serve it as a ring app
;; this is meant for development purposes.
(def app (stasis/serve-pages get-pages)) 
