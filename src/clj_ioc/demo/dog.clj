(ns clj-ioc.demo.dog)

(defn greet
  "IOC implementation of :greet function."
  ([] (greet nil))
  ([n] (str "Arf" (if n (str ", " n) "") ".")))

(defn scientific-name
  "IOC implementation of :scientific-name function."
  []
  "Canis lupus familiaris")
