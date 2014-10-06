(ns clj-ioc.demo.indirect
  (:require [clj-ioc.core :as ioc]))

(def ioc-key :indirect)
(def ioc-default-ns "clj-ioc.demo.human")
(def ioc-func-names [:greet :scientific-name])

;; initialization of ioc key, ioc default namespace, and ioc allowed function names
(ioc/register-ioc-namespace! ioc-key ioc-default-ns ioc-func-names true)

(defn set-namespace!
  "Sets the namespace via the string :ioc-ns with the optional :force? argument to force setting even if not all functions are resolvable."
  ([ioc-ns] (set-namespace! ioc-ns false))
  ([ioc-ns force?]
   (ioc/set-namespace! ioc-key ioc-ns force?)))

(defn hello
  ([] (hello nil))
  ([n]
   (ioc/call ioc-key :greet n)))

(defn greet
  ([] (greet nil))
  ([n]
   (ioc/call ioc-key :greet n)))

(defn scientific-name
  []
  (ioc/call ioc-key :scientific-name))

