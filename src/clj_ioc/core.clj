(ns clj-ioc.core)

(def ioc-namespaces (atom {}))

(defn keyword-or-string
  [s]
  (cond (keyword? s) (name s)
        :else (str s)))

(def kws (memoize keyword-or-string))

(defn kw
  [s]
  (cond (keyword? s) s
        (string? s) (keyword s)
        :else (keyword (str s))))

(defn nil-map-values
  "Returns the list of keys in map :m with nil values. If map has no nil values, returns nil."
  [m]
  (reduce #(if ((first %2) m) % (conj % (first %2))) nil m))

(defn resolve-func
  "Returns a resolved function given a namespace and function name, if present.  If function doesn't exist, returns nil."
  [fns fname]
  (require (symbol fns))
  (resolve (symbol (str fns "/" (name fname)))))

(defn nice-list
  "Returns a string of a given list of keywords/strings as strings separated by commas."
  [l]
  (clojure.string/join ", " (reduce #(conj % (kws %2)) [] l)))

(defn ioc-ns-map
  "Creates an IOC namespace map with resolved functions given function names and IOC namespace.  Throws an exception if missing a
   function AND the force? parameter is set to false?"
  ([ioc-ns ioc-func-names] (ioc-ns-map ioc-ns ioc-func-names false))
  ([ioc-ns ioc-func-names force?]
   (let [fm {}
         res (reduce #(assoc % %2 (resolve-func ioc-ns %2))
                     {}
                     ioc-func-names)
         missing-funcs (nil-map-values res)
         mfs (into #{} missing-funcs)]
     (if-not (empty? missing-funcs)
       (if force?
         {:ns ioc-ns
          :func-names ioc-func-names
          :funcs res
          :missing-funcs mfs}
         (throw (RuntimeException. (str "Aborted IOC namespace assignment to '" ioc-ns
                                        "' with required functions [" (nice-list ioc-func-names) "] due to missing function definitions ["
                                        (nice-list mfs) "]."))))
       {:ns ioc-ns
        :func-names ioc-func-names
        :funcs res}))))

(defn register-ioc-namespace!
  "Registers an IOC namespace given an :ioc-key, :ioc-seed-ns string namespace, :ioc-func-names list of function names as keywords, and
   optional :force? boolean (defaults to false) which will thrown an exception if one function is not defined in the namespace."
  ([ioc-key ioc-seed-ns ioc-func-names] (register-ioc-namespace! ioc-key ioc-seed-ns ioc-func-names false))
  ([ioc-key ioc-seed-ns ioc-func-names force?]
   (let [res (ioc-ns-map ioc-seed-ns ioc-func-names force?)]
     (swap! ioc-namespaces assoc ioc-key res))))

(defn unregister-ioc-namespace!
  "Unregisters IOC namespace given the IOC key :ioc-key."
  [ioc-key]
  (swap! ioc-namespaces dissoc ioc-key))

(defn set-namespace!
  "Sets an IOC namespace given an existing :ioc-key, :ioc-seed-ns string namespace, :ioc-func-names list of function names as keywords, and
   optional :force? boolean (defaults to false) which will thrown an exception if one function is not defined in the namespace."
  ([ioc-key ioc-ns] (set-namespace! ioc-key ioc-ns false))
  ([ioc-key ioc-ns force?]
   (let [lioc (-> @ioc-namespaces ioc-key)]
     (if lioc
       (do
         (swap! ioc-namespaces assoc ioc-key (ioc-ns-map ioc-ns (:func-names lioc) force?))
         (ioc-key @ioc-namespaces))))))

(defn call
  "Calls IOC function for a given :ioc-key and :fkey function key, given the :args list of arguments."
  [ioc-key fkey & args]
  (let [f (-> @ioc-namespaces ioc-key :funcs fkey)
        lns (-> @ioc-namespaces ioc-key :ns)]
    (if f
      (apply f args)
      (throw (RuntimeException. (str "Exception in " ioc-key " IOC call occurred due to no function '"
                              (name fkey) "' definition in namespace '" lns "' (" (str lns "/" (name fkey)) ") with "
                              (if (> (count args) 0) (str "arguments: " args) "no arguments")  "."))))))

;; (register-ioc-namespace! :text "cmis.utils.text" [:kws :comp-key])

;; (call :text :kws :freaknik)

;; (set-namespace! :text "cmis.utils.scratch")

;; (call :text :kws :freaknik)

;; (call :greeter2 :greet "Wanda")
;; (ioc-namespace! :greeter2 "scratch-ioc.logic.human" [:greet :freak])
;; (set-namespace! :greeter2 "scratch-ioc.logic.dog" true)
;; (call :greeter2 :greet "monkey")
;; (set-namespace! :greeter2 "scratch-ioc.logic.cat" true)
;; (call :greeter2 :greet "monkey")



(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
