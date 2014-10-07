# clj-ioc

A Clojure library designed to provide inversion-of-control (IOC) in an idiomatic manner.

## Rationale

[Inversion-of-control (IOC)](http://en.wikipedia.org/wiki/Inversion_of_control), aka dependcy injection (DI), is a very useful technique to provide
change in functionality without the need to change coding.

In Java, IOC is mostly done using Java interfaces, and Clojure does have the ability to
use Java interface.

However, the Protocol approach seems a bit heavy for simple IOC, but Protocols do offer a form of type safety (guard rails on the road) that `clj-ioc` (painted lines on the road) does not.

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
[org.clojars.clj-ioc/clj-ioc "0.1.2"]
```

### Demo Namespaces

**indirect.clj**
```clojure
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
user=> (require '[clj-ioc.demo.indirect :as indirect])
nil
user=> (ioc/get-ioc-namespace :indirect)
{:ns "clj-ioc.demo.human", :func-names [:greet :scientific-name], :funcs {:scientific-name #'clj-ioc.demo.human/scientific-name, :greet #'clj-ioc.demo.human/greet}}
user=> (indirect/greet)
"Hi."
user=> (indirect/greet "Abdul")
"Hi, Abdul."
user=> (indirect/scientific-name)
"Homo sapien"
user=> (indirect/set-namespace! "clj-ioc.demo.dog" true)
{:ns "clj-ioc.demo.dog", :func-names [:greet :scientific-name], :funcs {:scientific-name #'clj-ioc.demo.dog/scientific-name, :greet #'clj-ioc.demo.dog/greet}}
user=> (indirect/greet)
"Arf."
user=> (indirect/greet "Abdul")
"Arf, Abdul."
user=> (indirect/scientific-name)
"Canis lupus familiaris"
user=> (indirect/set-namespace! "clj-ioc.demo.cat")
RuntimeException Aborted IOC namespace assignment to 'clj-ioc.demo.cat' with required functions [greet, scientific-name] due to missing function definitions [scientific-name].  clj-ioc.core/ioc-ns-map (core.clj:53)
user=> (indirect/set-namespace! "clj-ioc.demo.cat" true)
{:ns "clj-ioc.demo.cat", :func-names [:greet :scientific-name], :funcs {:scientific-name nil, :greet #'clj-ioc.demo.cat/greet}, :missing-funcs #{:scientific-name}}
user=> (indirect/greet)
"Meow."
user=> (indirect/greet "Abdul")
"Meow, Abdul."
user=> (indirect/scientific-name)
RuntimeException Exception in :indirect IOC call occurred due to no function 'scientific-name' definition in namespace 'clj-ioc.demo.cat' (clj-ioc.demo.cat/scientific-name) with no arguments.  clj-ioc.core/call (core.clj:91)
user=> (indirect/set-namespace! "clj-ioc.demo.human")
{:ns "clj-ioc.demo.human", :func-names [:greet :scientific-name], :funcs {:scientific-name #'clj-ioc.demo.human/scientific-name, :greet #'clj-ioc.demo.human/greet}}
user=> (indirect/greet)
"Hi."
```
## Resources

### Links
* [Inversion-of-Control (IOC)](http://en.wikipedia.org/wiki/Inversion_of_control)

## License

Copyright © 2014 [AptPath LLC](http://aptpath.com)

Distributed under the Eclipse Public License, the same as [Clojure](http://clojure.org).
