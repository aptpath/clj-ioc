(ns clj-ioc.demo.dispatcher
  (:require [clj-ioc.core :as ioc]))

(def ^{:private true} ioc-key :dispatcher)
(def ^{:private true} ioc-default-ns "clj-ioc.demo.human")
(def ^{:private true} ioc-func-names [:greet :scientific-name])

;; initialization of ioc key, ioc default namespace, and ioc allowed function names
(ioc/register-namespace! ioc-key ioc-default-ns ioc-func-names true)

(defn set-namespace!
  "Sets the namespace via the string :ioc-ns with the optional :force? argument to force setting even if not all functions are resolvable."
  ([ioc-ns] (set-namespace! ioc-ns false))
  ([ioc-ns force?]
   (ioc/set-namespace! ioc-key ioc-ns force?)))

(defn hello
  "Function to show different function name is able to call an IOC function."
  ([] (hello nil))
  ([n]
   (ioc/call ioc-key :greet n)))

(defn greet
  "Function stub for calling :greet in the :dispatcher IOC mapping."
  ([] (greet nil))
  ([n]
   (ioc/call ioc-key :greet n)))

(defn scientific-name
  "Function stub for calling :scientific-name in the :dispatcher IOC mapping."
  []
  (ioc/call ioc-key :scientific-name))
