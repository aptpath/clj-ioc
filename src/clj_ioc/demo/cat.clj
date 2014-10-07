(ns clj-ioc.demo.cat)

(defn greet
  "IOC implementation of :greet function."
  ([] (greet nil))
  ([n] (str "Meow" (if n (str ", " n) "") ".")))

