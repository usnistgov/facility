(defproject gov.nist.mm/facility "0.1.0-SNAPSHOT"
  :description "Demo Production Facility Ontology Management System"
  :url "https://github.com/usnistgov/facilityh"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
   :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.immutant/immutant "2.0.2"]
                 [compojure "1.3.4"]
                 [ring/ring-devel "1.4.0"]
                 [ring/ring-core "1.4.0"]
                 [org.clojure/core.memoize "0.5.6"]
                 [clj-time "0.9.0"]
                 [cheshire "5.4.0"]
                 [environ "1.0.0"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [hiccup "1.0.5"]
                 [edu.ucdenver.ccp/kr-jena-core "1.4.17"]
                 [gov.nist/pod "0.1.0-SNAPSHOT"]] ; POD for development
  :repositories [["Immutant incremental builds"
                  "http://downloads.immutant.org/incremental/"]]
  :plugins [[lein-immutant "2.0.0"]]
  :main ^:skip-aot gov.nist.mm.facility
  :uberjar-name "facility-standalone.jar"
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :min-lein-version "2.4.0")
