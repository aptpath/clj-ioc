# clj-ioc

A Clojure library designed to provide inversion-of-control (IOC) in an idiomatic manner.

## Rationale

[Inversion-of-control (IOC)](http://en.wikipedia.org/wiki/Inversion_of_control), aka dependcy injection (DI), is a very useful technique to provide
change in functionality without the need to change coding.

In Java, IOC is mostly done using Java interfaces, and Clojure does have the ability to
use Java interface.

However, the Protocol approach seems a bit heavy for simple IOC, but Protocols do offer a form of type safety (guard rails on the road) than `clj-ioc` (painted lines on the road) does not.

The `clj-ioc` library provides a light-weight, simple, and powerful mechanism for IOC.

## IOC Uses

IOC is useful in providing flexibility to any application, such as allowing for different
database solutions for storing application data or allowing different authentication
options, all without changing core application code.

Another IOC use is to provide mock services while testing, such as eliminating the
need for an external test database by mocking up an internal test harness database or
any other external service that may not be available or convenient in the development,
testing, and/or continuous integration (CI) environments.

## Usage

### Dependencies

```clojure
[clj-ioc/clj-ioc "0.2.0"]
```

The previous clojars group `org.clojars.clj-ioc` is deprecated, please use the canonical `clj-ioc` group as shown above.

### Developer Notes
* some function names were changed between 0.1.6 and 0.2.0 to be more consistent and concise

### Demo Namespaces

**dispatcher.clj**
```clojure
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
```

**human.clj**
```clojure
(ns clj-ioc.demo.human)

(defn greet
  ([] (greet nil))
  ([n] (str "Hi" (if n (str ", " n) "") ".")))

(defn scientific-name
  []
  "Homo sapien")
```

**dog.clj**
```clojure
(ns clj-ioc.demo.dog)

(defn greet
  ([] (greet nil))
  ([n] (str "Arf" (if n (str ", " n) "") ".")))

(defn scientific-name
  []
  "Canis lupus familiaris")
```

**cat.clj**
```clojure
(ns clj-ioc.demo.cat)

(defn greet
  ([] (greet nil))
  ([n] (str "Meow" (if n (str ", " n) "") ".")))
```

### REPL Demo

```clojure
user=> (require '[clj-ioc.core :as ioc])
nil
user=> (require '[clj-ioc.demo.dispatcher :as dispatcher])
nil
user=> (ioc/get-namespace :dispatcher)
{:ns "clj-ioc.demo.human", :func-names [:greet :scientific-name], :funcs {:scientific-name #'clj-ioc.demo.human/scientific-name, :greet #'clj-ioc.demo.human/greet}}
user=> (dispatcher/greet)
"Hi."
user=> (dispatcher/greet "Abdul")
"Hi, Abdul."
user=> (dispatcher/scientific-name)
"Homo sapien"
user=> (dispatcher/set-namespace! "clj-ioc.demo.dog")
{:ns "clj-ioc.demo.dog", :func-names [:greet :scientific-name], :funcs {:scientific-name #'clj-ioc.demo.dog/scientific-name, :greet #'clj-ioc.demo.dog/greet}}
user=> (dispatcher/greet)
"Arf."
user=> (dispatcher/greet "Abdul")
"Arf, Abdul."
user=> (dispatcher/scientific-name)
"Canis lupus familiaris"
user=> (dispatcher/set-namespace! "clj-ioc.demo.cat") ;; this will fail
RuntimeException Aborted IOC namespace assignment to 'clj-ioc.demo.cat' with required functions [greet, scientific-name] due to missing function definitions [scientific-name].  clj-ioc.core/ioc-ns-map (core.clj:53)
user=> (dispatcher/set-namespace! "clj-ioc.demo.cat" true) ;; added force? to true to force namespace even if missing functions
{:ns "clj-ioc.demo.cat", :func-names [:greet :scientific-name], :funcs {:scientific-name nil, :greet #'clj-ioc.demo.cat/greet}, :missing-funcs #{:scientific-name}}
user=> (dispatcher/greet)
"Meow."
user=> (dispatcher/greet "Abdul")
"Meow, Abdul."
user=> (dispatcher/scientific-name)
RuntimeException Exception in :dispatcher IOC call occurred due to no function 'scientific-name' definition in namespace 'clj-ioc.demo.cat' (clj-ioc.demo.cat/scientific-name) with no arguments.  clj-ioc.core/call (core.clj:91)
user=> (dispatcher/set-namespace! "clj-ioc.demo.human")
{:ns "clj-ioc.demo.human", :func-names [:greet :scientific-name], :funcs {:scientific-name #'clj-ioc.demo.human/scientific-name, :greet #'clj-ioc.demo.human/greet}}
user=> (dispatcher/greet)
"Hi."
```

## IOC Function Coverage

There are a variety of functions in the clj-ioc core namespace to check for IOC function coverage.

The best check is to register the namespace to an IOC key using a function name list and not force the registration if any functions are not resolvable (found) in the namespace.

```clojure
(ioc/register-ioc-namespace! :foo "clj-ioc.demo.cat" [:greet :scientific-name])
```
will throw a `RuntimeException` since the namespace `clj-ioc-.demo.cat` does NOT define the function `scientific-name` and the optional `force?` fourth argument defaults to `false`.

```clojure
(ioc/register-ioc-namespace! :foo "clj-ioc.demo.cat" [:greet :scientific-name] true)
```
will allow the registration even though `clj-ioc.demo.cat` does not define the `scientific-name` function and will not throw an exception since the `force?` fourth argument is set to `true`.

However, now the `:foo` IOC namespace does not have complete coverage of the IOC functions and any call to the `:foo` IOC namespace via the `ioc/call` to the missing function (in this case `scientific-name`) will result in a thrown exception.

It is best practices to *NOT* force registration of partial coverage namespaces (those namespaces without complete resolution of ALL IOC functions).

## Testing IOC Function Coverage

Testing for IOC functional coverage may be done by:

```clojure
(ioc/coverages)
```

If the function returns nil, then all registered IOC namespaces provide complete coverage for the registered function names.

However, if a map is returned (keyed using the IOC key), then those IOC keyed namespace in the result map do NOT have complete IOC function coverage.

Individual IOC registered IOC keys may be checked for missing functions:

```clojure
(ioc/coverage :ioc-key)
```

If the result is nil, the IOC key'd namespace has complete coverage (or the IOC key is not registered).  However, if the result is the registered map (with the resolved functions redacted), then the :missing-funcs vector holds the missing function names.

NOTE: `(ioc/coverages)` is the same call as `(ioc/all-missing-functions)`; and `(ioc/coverage :ioc-key)` is the same call as `(ioc/missing-functions :ioc-key)`.

## Getting IOC namespace mappings
To get a map of mapped namespaces:

```clojure
(ioc/get-namespaces)
```

will result in `nil` if no IOC namespaces are registered.

Or a map with the IOC key as the key and a string containing the mapped namespace as the value.

For example, if the :dispatcher and :foo IOC keys have mapped IOC namespaces:

```clojure
{:dispatcher "clj-ioc.demo.human"
 :foo "clj-ioc.demo.cat"}
```

To get the mapped namespace for an IOC key:

```clojure
(ioc/get-namespace :dispatcher)
```

The result is `nil` if there is no mapping, or a namespace as a string.

```clojure
(ioc/get-namespace :dispatcher)
"clj-ioc.demo.human"
```

```clojure
(ioc/get-namespace :no-mapping-to-this-key)
nil
```
## Resources

### Links
* [Inversion-of-Control (IOC)](http://en.wikipedia.org/wiki/Inversion_of_control)

## License

Copyright © 2014 [AptPath LLC](http://aptpath.com)

Distributed under the Eclipse Public License, the same as [Clojure](http://clojure.org).
