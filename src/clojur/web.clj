(ns clojur.web
  (:require [stasis.core :as stasis]
            [hiccup.page :refer [html5]]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [markdown.core :as markdown]
            [optimus.assets :as assets]
            [optimus.export]
            [optimus.link :as link]
            [optimus.optimizations :as optimizations]
            [optimus.prime :as optimus]
            [optimus.strategies :refer [serve-live-assets]]))

(defn layout-page
  ""
  [request page]
  (html5 
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1.0"}]
     [:title "Clojur.es - Clojure en español"]
     [:link {:rel "stylesheet" :href (link/file-path request "/styles/styles.css")}]
     [:link {:rel "stylesheet" :href (link/file-path request "/js/highlight/styles/atelier-dune-light.css")}]]
    [:body
     [:div.logo "clojur.es"]
     [:div.body page]
     [:script {:src (link/file-path request "/js/highlight/highlight.pack.js")}]
     [:script "hljs.initHighlightingOnLoad();"]]))

(defn about-page
  [request]
  (layout-page request (slurp (io/resource "partials/about.html"))))

(defn get-assets
  "make optimus load the assets"
  []
  (assets/load-assets "assets" [#".*"]))


(defn partial-pages
  "Gather all partials and add its layout"
  [pages]
  (zipmap (keys pages)
          (map (fn [page]
                 (fn [request] (layout-page request page)))
               (vals pages))))

(defn- md-page 
  [request page]
  (layout-page request (markdown/md-to-html-string page)))

;; this could be improved, can we do the layout in markdown?
(defn markdown-pages
  ""
  [pages]
  (zipmap (map #(string/replace % #"\.md$" "/") (keys pages))
          (map (fn [page]
                 (fn [request]
                   (md-page request page)))
               (vals pages))))

(defn prepare-page
  [page request] ;; this fn used to contain a thread operator, but we dont need it anymore
  (if (string? page)
    page
    (page request)))

(defn prepare-pages
  [pages]
  (zipmap (keys pages)
          (map #(partial prepare-page %) (vals pages))))

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
(def app
  (optimus/wrap (stasis/serve-pages (prepare-pages (get-pages)))
                get-assets
                optimizations/all
                serve-live-assets)) 

;; static build

(def export-dir "dist")

(defn export
  ""
  []
  (let [assets (optimizations/all (get-assets) {})]
    (stasis/empty-directory! export-dir)
    (optimus.export/save-assets assets export-dir)
    (stasis/export-pages (get-pages) export-dir {:optimus-assets assets})))
