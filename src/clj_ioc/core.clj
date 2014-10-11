(ns clj-ioc.core
  (:require [clojure.set :as set]))

(def ^{:private true} ioc-namespaces (atom {}))

(defn- keyword-or-string
  [s]
  (cond (keyword? s) (name s)
        :else (str s)))

(def ^{:private true} kws (memoize keyword-or-string))

(defn- kw
  [s]
  (cond (keyword? s) s
        (string? s) (keyword s)
        :else (keyword (str s))))

(defn- vec-
  "Returns the vector resulting in v1 minus v2."
  [v1 v2]
  (into [] (set/difference (into #{} v1) (into #{} v2))))

(defn- nil-map-values
  "Returns the list of keys in map :m with nil values. If map has no nil values, returns nil."
  [m]
  (reduce #(if ((first %2) m) % (conj % (first %2))) nil m))

(defn- nice-list
  "Returns a string of a given list of keywords/strings as strings separated by commas."
  [l]
  (clojure.string/join ", " (reduce #(conj % (kws %2)) [] l)))

(defn resolve-func
  "Returns a resolved function given a namespace and function name, if present.  If function doesn't exist, returns nil."
  [fns fname]
  (require (symbol (kws fns)))
  (resolve (symbol (str (kws fns) "/" (kws fname)))))

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
         mfs (into [] missing-funcs)]
     (if-not (empty? missing-funcs)
       (if force?
         {:ns (kws ioc-ns)
          :func-names ioc-func-names
          :funcs res
          :resolved-funcs (vec- ioc-func-names mfs)
          :missing-funcs mfs}
         (throw (RuntimeException. (str "Aborted IOC namespace assignment to '" (kws ioc-ns)
                                        "' with required functions [" (nice-list ioc-func-names) "] due to missing function definitions ["
                                        (nice-list mfs) "]."))))
       {:ns (kws ioc-ns)
        :func-names ioc-func-names
        :resolved-funcs ioc-func-names
        :funcs res}))))

(defn mappings
  "Returns the current ioc-namespaces map."
  []
  @ioc-namespaces)

(defn mapping
  "Returns the IOC namespace map for entry with ioc-key."
  [ioc-key]
  (@ioc-namespaces ioc-key))

(defn- get-missing-funcs
  "Arguments:
    ioc-key (optional) - the IOC key for the IOC mapping missing function retrieval
                         if not nil, return only the missing functions info map for the ioc-key'd IOC mapping.
                         if nil, return all IOC mappings with missing functions as IOC mapping.

  Returns:
    nil - if there are no IOC mapping(s) with missing (unresolvable) functions
    single IOC info mapping - if there is an ioc-key AND the ioc-key'd IOC mapping is missing functions
    IOC key'd map with IOC info mapping - if there is no ioc-key AND at least one (1) IOC mapping is missing functions

  IOC missing info mapping keys:
     :ns - namespace
     :func-names - vector of function names
     :resolved-funcs - vector of resolved (successful) function names
     :missing-funcs - vector of missing function names."
  ([] (get-missing-funcs nil))
  ([ioc-key]
   (if ioc-key
     (if (:missing-funcs (@ioc-namespaces ioc-key))
       (dissoc (@ioc-namespaces ioc-key) :funcs))
     (reduce #(if (:missing-funcs (@ioc-namespaces %2))
                (assoc % %2 (dissoc (@ioc-namespaces %2) :funcs))
                %)
             nil
             (keys @ioc-namespaces)))))

(defn all-missing-functions
  "Returns all IOC namespaces as maps with IOC keys as keys and list of missing function names.
   If an IOC key has no missing functions (meaning the namespace has complete coverage of
   the IOC functions), then the IOC key is not part of the result.
   If the result is nil, then no registered IOC keys are missing any functions and there is
   complete coverage of all registered IOC keys."
  []
  (get-missing-funcs))

(defn missing-functions
  "Returns a list of missing functions (not defined in the registered namespace) of the
   registered ioc-key parameters.  Returns nil is the ioc-key doesn't exist in the
  registry and/or if the ioc-key'd namespace has complete coverage of the IOC functions."
  [ioc-key]
  (get-missing-funcs ioc-key))

(defn coverage
  "Convenience function that calls missing-functions [ioc-key]."
  [ioc-key]
  (missing-functions ioc-key))

(defn coverages
  "Convenience function that calls all-missing-functions []."
  []
  (all-missing-functions))

(defn register-namespace!
  "Registers an IOC namespace given an :ioc-key, :ioc-seed-ns string namespace, :ioc-func-names list of function names as keywords, and
   optional :force? boolean (defaults to false) which will thrown an exception if one function is not defined in the namespace.
   If successful, returns the entire @ioc-namespaces atom map.

  Example:
    (register-namespace! :storage :prj-name.store.mongo [:create :retrieve :update :delete] false)
    ;; register ioc mapping for ioc-key :storage and namespace :prj-name.store.mongo with IOC function names [:create :retrieve :update :delete]
    ;; and do not force? (false) to register if there are any functions in the function names list [:create :retrieve :update :delete] not resolvable
    ;; in the given namespace (:prj-name.store.mongo)."
  ([ioc-key ioc-seed-ns ioc-func-names] (register-namespace! ioc-key ioc-seed-ns ioc-func-names false))
  ([ioc-key ioc-seed-ns ioc-func-names force?]
   (let [res (ioc-ns-map ioc-seed-ns ioc-func-names force?)]
     (swap! ioc-namespaces assoc ioc-key res))))

(defn unregister-namespace!
  "Unregisters IOC namespace given the IOC key :ioc-key."
  [ioc-key]
  (swap! ioc-namespaces dissoc ioc-key))

(defn set-namespace!
  "Sets an IOC namespace given an existing :ioc-key, :ioc-seed-ns string namespace, :ioc-func-names list of function names as keywords, and
   optional :force? boolean (defaults to false) which will thrown an exception if one function is not defined in the namespace."
  ([ioc-key ioc-ns] (set-namespace! ioc-key ioc-ns false))
  ([ioc-key ioc-ns force?]
   (let [lioc (@ioc-namespaces ioc-key)]
     (if lioc
       (do
         (swap! ioc-namespaces assoc ioc-key (ioc-ns-map ioc-ns (:func-names lioc) force?))
         (ioc-key @ioc-namespaces))))))

(defn get-namespace
  "Returns the mapped namespace of the ioc-key parameter, nil if no mapping
  for the provide ioc-key."
  [ioc-key]
  (:ns (@ioc-namespaces ioc-key)))

(defn get-namespaces
  "Returns a mapping of all ioc-keys with the ioc-key as the key and the mapped namespace
  as the value."
  []
  (reduce #(assoc % %2 (:ns (@ioc-namespaces %2))) nil (keys @ioc-namespaces)))

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

