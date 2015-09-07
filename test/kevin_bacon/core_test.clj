(ns kevin-bacon.core-test
  (:require [clojure.test :refer :all]
            [kevin-bacon.core  :as kb]))

(deftest a-test

    (kb/get-links "Iron_pillar_of_Delhi"))
