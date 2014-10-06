# clj-ioc

A Clojure library designed to provide inversion-of-control (IOC) in an idiomatic manner.

## Usage

### Dependencies

```clojure
[org.clojars.clj-ioc/clj-ioc "0.1.0"]
```

### REPL Demo

```clojure
user=> (require '[clj-ioc.core :as ioc])
nil
user=> (require '[clj-ioc.demo.indirect :as indirect])
nil
user=> (indirect/greet)
"Hi."
user=> (indirect/greet "Abdul")
"Hi, Abdul."
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

user=>

```

## License

Copyright © 2014 AptPath LLC

Distributed under the Eclipse Public License, the same as Clojure.
