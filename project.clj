(defproject gov.nist.mm/facility "0.1.0-SNAPSHOT"
  :description "Demo Production Facility Ontology Management System"
  :url "https://github.com/usnistgov/facility"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
   :dependencies [[org.clojure/clojure "1.9.0"]
                  [org.immutant/immutant "2.1.4"]
                  [compojure "1.5.0"]
                  [ring/ring-devel "1.4.0"]
                  [ring/ring-core "1.4.0"]
                  [org.clojure/core.memoize "0.5.9"]
                  [clj-time "0.11.0"]
                  [cheshire "5.6.1"]
                  [environ "1.0.2"]
                  [org.clojure/java.jdbc "0.5.8"]
                  [hiccup "1.0.5"]
                  [edu.ucdenver.ccp/kr-jena-core "1.4.19"]]
;;; :repositories [["Immutant incremental builds" ; lein gives "Tried to use insecure HTTP repository without TLS."
;;;                 "http://downloads.immutant.org/incremental/"]]
  :plugins [[lein-immutant "2.0.0"]]
  :main ^:skip-aot gov.nist.mm.facility
  :uberjar-name "facility-standalone.jar"
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})

