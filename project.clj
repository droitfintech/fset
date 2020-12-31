(defproject fset "0.1.0"
  :description "A faster implementation of clojure.set and additional relational algebra functions."
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :repl-options {:init-ns fset.core
                 :timeout 5000000}
  :global-vars {*warn-on-reflection* true}
  :profiles
  {:dev {:jvm-opts
         [
          ; "-agentpath:/Applications/VisualVM.app/Contents/Resources/visualvm/visualvm/lib/deployed/jdk16/mac/libprofilerinterface.jnilib=/Applications/VisualVM.app/Contents/Resources/visualvm/visualvm/lib,5140"
          ; "-Xverify:none"
          ]
         :dependencies [[org.clojure/test.check "1.1.0"]
                        [criterium "0.4.6"]]}}
  :repositories {"releases" {:url "https://nexus-repo.dit.droitfintech.net/repository/droit-repo-release/"
                             :sign-releases false
                             :no-auth true}
                 "snapshots" {:url "https://nexus-repo.dit.droitfintech.net/repository/droit-repo-snapshot/"
                              :no-auth true}})
