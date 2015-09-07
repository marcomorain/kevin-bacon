(ns kevin-bacon.core
  (:require
    [clojure.java.io :refer (file)]
    [clojure.string :refer (escape)]
    [clojure.set :as set]))

(def ^:dynamic *base* "https://en.wikipedia.org/wiki/")

(defn timer []
  (let [start (System/nanoTime)]
    (fn []
      (/ (- (System/nanoTime) start) 1000000000))))

(defn link [p]
  (str *base* p))

(def cache-base "resources/wiki/")

(defn cache-path [p]
  ;; TODO - can get collisions
  (str cache-base (escape p {\/ \-})))

(defn download [p]
  (printf "Downloading %s\n" p)
  (flush)
  (let [t (timer)
        content (slurp (link p))]
    (printf "Downloaded in %f seconds\n" (double (t)))
    content))

(defn with-local-caching [f]
  (fn [p]
   (let [local (cache-path p)]
    (if (.exists (file local))
      (do
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

(defn search*
  [x a [b & frontier] visited discovered]
  (printf "frontier=%s visited=%s node=%s\n"
            (count frontier) (count visited) b )
  (let [new-links (set/difference (get-links b) visited #{b})
        discoveries (zipmap new-links (repeat b))
        d (merge discovered discoveries)]
    (printf "frontier=%s visited=%s node=%s (%s new)\n"
            (count frontier) (count visited) b (count new-links))
    (if (contains? new-links a)
      (do
        d)
      (recur (inc x)
             a
             (concat frontier new-links)
             (conj visited b)
             d))))

(defn search [a b]
  (if (= a b)
    {}
    (let [m (search* 0 a [b] #{} {})]
      (loop [needle a
             path ()]
        (if-let [node (get m needle)]
          (recur node (conj path needle))
          (conj path needle))))))
