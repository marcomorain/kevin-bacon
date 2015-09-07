(ns kevin-bacon.core
  (:require
    [clojure.java.io :refer (file)]
    [clojure.set :as set]))

(def ^:dynamic *base* "https://en.wikipedia.org/wiki/")

(defn link [p]
  (str *base* p))

(def cache-base "resources/wiki/")

(defn cache-path [p]
  (str cache-base p))

(defn download [p]
  (printf "Downloading %s\n" p)
  (let [content (slurp (link p))]
    (printf "Download OK\n")
    content))

(defn with-local-caching [f]
  (fn [p]
   (let [local (cache-path p)]
    (if (.exists (file local))
      (do
        (printf "Local cache hit: %s\n" p)
        (slurp local))
      (let [content (f p)]
        (spit local content)
        content)))))

(defn try-download [p]
  (try
    ((with-local-caching download) p)
    (catch Exception e
      (printf "Error %s\n" (.getMessage e))
      "")))

(defn get-links[p]
  (->> p
       try-download
       (re-seq #"href=\"/wiki/(.[^\"]*)")
       (map second)
       (remove (partial re-find #":"))
       set))

(def graph (atom {}))


(defn children [n]
  (get-links n))

(defn search*
  [a [b & frontier] visited path]
  (printf "path=%s a=%s b=%s frontier=%s visited=%s\n"
         path a b (count frontier) (count visited))
  (let [links (get-links b)]
    (cond
      (= a b) path
      (contains? links a) (conj path b)
      true
      (recur a
             (concat frontier (set/difference links visited))
             (conj visited b)
             ;(conj path b)
             ()
             ))))

(defn search [a b]
  (search* a [b] #{} ()))
