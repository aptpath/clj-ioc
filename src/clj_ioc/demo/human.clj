(ns clj-ioc.demo.human)

(defn greet
  "IOC implementation of :greet function."
  ([] (greet nil))
  ([n] (str "Hi" (if n (str ", " n) "") ".")))

(defn scientific-name
  "IOC implementation of :scientific-name function."
  []
  "Homo sapien")
