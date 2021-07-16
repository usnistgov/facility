(ns gov.nist.mm.facility
  (:require [immutant.web             :as web]
            [immutant.web.middleware  :as immutant]
            [clojure.java.io          :as io]
            [compojure.route          :as route]
            [compojure.core     :refer (ANY GET defroutes)]
            [ring.util.response :refer (response #_redirect content-type)]
            [ring.util.codec    :refer (url-encode url-decode)]
            [environ.core       :refer (env)]
            [hiccup.core :refer (html)]
            [clojure.pprint :refer (pprint)]
            [clojure.string :as str]
            [edu.ucdenver.ccp.kr.kb :as kb]
            [edu.ucdenver.ccp.kr.rdf :as rdf]
            [edu.ucdenver.ccp.kr.sparql :as sparql]
            [edu.ucdenver.ccp.kr.jena.kb]
            [cheshire.core])
  (:import java.net.URI
           java.io.ByteArrayInputStream)
  (:gen-class))

(defn operations-uri
  "Return a java.net.URI for the argument string."
  [s & {:keys [base-str] :or {base-str "http://modelmeth.nist.gov/operations#"}}]
  (->> s
       (str base-str)
       java.net.URI/create))

;;; http://stackoverflow.com/questions/3644125/clojure-building-of-url-from-constituent-parts
(defn make-query-string [m & [encoding]]
  (let [s #(if (instance? clojure.lang.Named %) (name %) %)
        enc (or encoding "UTF-8")]
    (->> (for [[k v] m]
           (str (url-encode (s k) enc)
                "="
                (url-encode (str v) enc)))
         (interpose "&")
         (apply str))))

(defn build-url [url-base query-map & [encoding]]
  (str url-base "?" (make-query-string query-map encoding)))

;;; POD TODO: load DOLCE Lite Plus (DLP). (I am currently only testing it; See operations/ResourceState.)
(defn load-kb
  "Return a rdf/KB object with input loaded."
  [& streams]
  (let [kb (kb/kb :jena-mem)]
    (dorun (map #(rdf/load-rdf kb (:stream %) (:format %)) streams))
    (rdf/synch-ns-mappings kb)))

(def operations-kb "The RDF database" nil)
  
(defn echo
  "Echos the request back as a string."
  [request]
  (-> (response (with-out-str (pprint request)))
    (content-type "text/plain")))

(defn wrap-content-type 
  "Add to headers on response. (See ring-clojure/ring 'Concepts' page on GitHub.)"
  [handler content-type]
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers "Content-Type"] content-type))))

(defn active-tab
  "Return HTML div metaltop-teal tabs, marking TAB as active."
  [ & {:keys [tab] :or {tab :concepts}}]
  `[:ul
    ~@(map (fn [[k v]]
             (if (= k tab)
               `[:li {:class "active"} [:a {:href ~(str "/FacilitySearch/" (name k))} ~v]]
               `[:li                   [:a {:href ~(str "/FacilitySearch/" (name k))} ~v]]))
           {:concepts "Concepts", :search "Search"})])

;;; ToDo:
;;;   - Add parameters (title, at least. See pod-utils/html-utils)
;;;   - Add a real style sheet (which could be a parameter).
(defmacro app-page-wrapper
  "Wrap pages with stylesheet, start session, etc."
  [args & body]
  `(->
    (response 
     (html [:html {:lang "en"}
            [:head [:title "Production Facility"]
             [:meta {:http-equiv "content-type" :content "text/html" :charset="iso-8859-1"}]
             [:link {:rel "stylesheet" :type "text/css" :href "/style.css"}]] ; Really need /style.css STRANGE!
            [:div {:id "metaltop-teal"} ~(active-tab :tab (:tab args))]
            [:body ~@body]]))
    (content-type "text/html")))

(defn empty-of
  "Return an empty copy of type S."
  [s]
  (cond (vector? s) []
        (list? s) '()
        (map? s) {}
        (set? s) #{}))

(defn seq-equal-length
  "Return vectors or lists of equal length, adding elements ELEM to end of shorter, if necessary."
  [l1 l2 elem]
  (let [c1 (count l1)
        c2 (count l2)
        make (if (vector? l1) vec list*)] 
    (cond (= c1 c2) (vector l1 l2)
          (> c1 c2) (vector l1 (make (concat l2 (into (empty-of l2) (repeatedly (- c1 c2) (fn [] elem))))))
          (< c1 c2) (vector    (make (concat l1 (into (empty-of l1) (repeatedly (- c2 c1) (fn [] elem))) l2))))))

(def diag (atom nil))

(defn owl-class-lookup-url
  "Return a URL for an owl:Class lookup."
  [resource & {:keys [root] :or {root "concepts/definition"}}]
  (let [[success? class-name] (->> resource name str (re-matches #"^.*#(.*)$"))]
    (when success?
      `[:a {:href ~(str root "?name=" (url-encode resource))} ~class-name])))

(defn concepts-pg
  "Handle page for concepts tab."
  [_request]
  (app-page-wrapper {:tab :concepts}
     (let [params (->> `((?/x rdfs/subClassOf ~(operations-uri "OperationsDomainConcept")))
                       (sparql/query operations-kb)
                       (map '?/x)
                       sort
                       (map owl-class-lookup-url))]
      (html
        [:h1 "Top-level Concepts"]
        `[:ul
          ~@(map (fn [d1] [:li d1]) params)]))))

(defn search-pg
  "Handle page for search tab."
  [_request]
  (app-page-wrapper {:tab :search}
                    "All: Nothing here yet."))

(defn page-tab
  "Return the keyword indicating the tab on which the page should be displayed."
  [request & {:keys [default] :or {default :concepts}}]
  (if-let [tab (:tab (:params request))]
    (keyword tab)
    default))

(defn concept-desc-pg
  "Describe an owl:Class."
  [request]
  (app-page-wrapper {:tab (page-tab request)}
      (str "Got it: params = " (:params request) " query-string = " (url-decode (:query-string request)))))

(defroutes routes
  (GET "/" [] concepts-pg)
  (GET "/FacilitySearch/:tab" [tab] 
       (cond (= tab "concepts") concepts-pg
             (= tab "search" )  search-pg))
  (GET "/FacilitySearch/:tab/concept*" [_tab] concept-desc-pg) 
  (route/resources "/") ; This one gets used for static pages in resources/public
  (ANY "*" [] echo)) ; Good for diagnostics

;;;  (-main :port 3034)
(defn -main
  "Main is the application main routine, also called in development using start."
  [& {:as args}]
  (alter-var-root
   (var operations-kb)
   (fn [_]
     (load-kb {:stream (ByteArrayInputStream. (.getBytes (slurp (io/resource "operations.ttl"))))
               :format :turtle}
              {:stream (ByteArrayInputStream. (.getBytes (slurp (io/resource "modeling.ttl"))))
               :format :turtle})))
  (web/run
    (-> routes
      (immutant/wrap-session {:timeout 20})
      (immutant/wrap-development)) ; POD added - saw it in the source, looked interesting.
    (merge {"host" (env :demo-web-host), "port" (env :demo-web-port)}
           args)))

(defn start []
  (-main :port 3034))

(defn stop []
  (web/stop (-main :port 3034)))

(defn restart []
  (stop)
  (start))


