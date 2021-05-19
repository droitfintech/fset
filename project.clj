(defproject fset "0.1.1"
  :description "A faster implementation of clojure.set and additional relational algebra functions."
  :url "https://github.com/droitfintech/fset"
  :license {:name "EPL-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :repl-options {:init-ns fset.core
                 :timeout 5000000}
  :global-vars {*warn-on-reflection* true}
  :profiles
  {:dev {:dependencies [[org.clojure/test.check "1.1.0"]
                        [criterium "0.4.6"]]}})
