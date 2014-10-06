(ns clj-ioc.demo.dog)

(defn greet
  ([] (greet nil))
  ([n] (str "Arf" (if n (str ", " n) "") ".")))

(defn scientific-name
  []
  "Canis lupus familiaris")
