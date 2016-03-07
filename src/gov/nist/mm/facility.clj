(ns gov.nist.mm.facility
  (:require [immutant.web             :as web]
            [immutant.web.async       :as async]
            [immutant.web.sse         :as sse]
            [immutant.web.middleware  :as immutant]
            [compojure.route          :as route]
            [compojure.core     :refer (ANY GET defroutes)]
            [ring.util.response :refer (response redirect content-type)]
            [ring.util.codec    :refer (url-encode url-decode)]
            [environ.core       :refer (env)]
            [hiccup.core :refer (html)]
            [clojure.pprint :refer (cl-format pprint)]
            [clojure.string :as str]
            [edu.ucdenver.ccp.kr.kb :refer :all]
            [edu.ucdenver.ccp.kr.rdf :refer :all]
            [edu.ucdenver.ccp.kr.sparql :refer :all]
            [edu.ucdenver.ccp.kr.jena.kb])
  (:import java.net.URI
           java.io.ByteArrayInputStream)
  (:gen-class))

;;; TODO:
;;;   DONE - Try to remove package specification for hiccup.
;;;   DONE - Find Page Design css etc. 
;;;   DONE - Fix namespaces (gov.nist.mm.facility.core) 
;;;        - Implement a process (ontology) page
;;;        - Implement a product (process plan, CAD model) page

;(use 'clojure.repl) ; POD Temporary. For use of doc.

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

(defn load-kb
  "Return a KB object with input loaded."
  [& files]
  (let [kb (kb :jena-mem)]
    (dorun (map #(load-rdf kb (ByteArrayInputStream. (.getBytes (slurp %))) :turtle) files))
    (synch-ns-mappings kb)))

(def mfg-kb (load-kb (clojure.java.io/resource "manufacturing.ttl")
                     (clojure.java.io/resource "toplevel-taxonomies.ttl")))

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

(defn- active-tab 
  [ & {:keys [tab] :or {tab :process}}]
  "Return HTML div metaltop-teal tabs, marking TAB as active."
  `[:ul
    ~@(map (fn [[k v]]
             (if (= k tab)
               `[:li {:class "active"} [:a {:href ~(str "/FacilitySearch/" (name k))} ~v]]
               `[:li                   [:a {:href ~(str "/FacilitySearch/" (name k))} ~v]]))
          {:process "Process", :product "Product", :equip "Equipment", :state "System State", :all "Unconstrained"})])

;;; ToDo:
;;;   - Add parameters (title, at least. See pod-utils/html-utils)
;;;   - Add a real style sheet (which could be a parameter).
(defmacro app-page-wrapper
  "Wrap pages with stylesheet, start session, etc."
  [args & body]
  `(->
    (response 
     (html [:html {:lang "en"}
            [:head [:title "Facility Search"]
             [:meta {:http-equiv "content-type" :content "text/html" :charset="iso-8859-1"}]
             [:link {:rel "stylesheet" :type "text/css" :href "/style.css"}]] ; Really need /style.css STRANGE!
            [:div {:id "metaltop-teal"} ~(active-tab :tab (:tab args))]
            [:body ~@body]]))
    (content-type "text/html")))

(defn empty-of 
  [s]
  "Return an empty copy of type S."
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

;;; POD Use url-params http://briancarper.net/clojure/compojure-doc.html
(defn owl-class-url 
  "Return a URL for an owl:Class."
  [obj & {:keys [root] :or {root "process/concept"}}]
  `[:a {:href ~(str root "?name=" (url-encode obj))} ~(str (name obj))])

#_(defn- process-search-pg
  [request]
  "Handle page for process tab."
  (app-page-wrapper {:tab :process}
    (let [params (query mfg-kb '((?/x rdfs/subClassOf mfg/MachiningProcessParameter)))
          mchars (query mfg-kb '((?/x rdfs/subClassOf mfg/MachineCharacteristic)))
          params-url (map owl-class-url (map '?/x params))
          mchars-url (map owl-class-url (map '?/x mchars))
          [params mchars] (seq-equal-length params-url mchars-url " ")]
      (html
      `[:table
        [:tr [:th "Process Parameters"] [:th "Machine Characteristics"]]
        ~@(map (fn [d1 d2] [:tr [:td d1] [:td d2]]) params mchars)]))))

(defn- process-search-pg
  [request]
  "Handle page for process tab."
  (app-page-wrapper {:tab :process}
    (let [params (query mfg-kb '((?/x rdfs/subClassOf mfg/MachiningProcessParameter)))
          params-url (map owl-class-url (map '?/x params))]
      (html
        [:h1 "Process Characteristics"]
        `[:ul
          ~@(map (fn [d1] [:li d1]) params-url)]))))

(defn- product-search-pg
  [request]
  "Handle page for product tab."
  (app-page-wrapper {:tab :product}
   "Product: Nothing here yet."))

(defn- equipment-search-pg
  [request]
  "Handle page for equipment tab."
  (app-page-wrapper {:tab :equip}
     (let [mchars (query mfg-kb '((?/x rdfs/subClassOf mfg/MachineCharacteristic)))
           mchars-url (map owl-class-url (map '?/x mchars))]
       (html
        [:h1 "Equipment Characteristics"]
        `[:ul
          ~@(map (fn [d1] [:li d1]) mchars-url)]))))

(defn- state-search-pg
  [request]
  "Handle page for facility state tab."
  (app-page-wrapper {:tab :state}
   "State: Nothing here yet."))

(defn- all-search-pg
  [request]
  "Handle page for unconstraint search tab."
  (app-page-wrapper {:tab :all}
   "All: Nothing here yet."))

(defn page-tab 
  [request & {:keys [default] :or {default :process}}]
  "Return the keyword indicating the tab on which the page should be displayed."
  (if-let [tab (:tab (:params request))]
    (keyword tab)
    default))

(defn- concept-desc-pg
  [request]
  "Describe an owl:Class."
  (app-page-wrapper {:tab (page-tab request)}
      (str "Got it: params = " (:params request) " query-string = " (url-decode (:query-string request)))))

(defroutes routes
  (GET "/" [] process-search-pg)
  (GET "/FacilitySearch/:tab" [tab] 
       (cond (= tab "process") process-search-pg
             (= tab "product") product-search-pg
             (= tab "equip"  ) equipment-search-pg
             (= tab "state"  ) state-search-pg
             (= tab "all"    ) all-search-pg))
  (GET "/FacilitySearch/:tab/concept*" [tab] concept-desc-pg) ; /FacilitySearch/process/concept?name=am-model/ModelParameter
  (route/resources "/") ; This one gets used for static pages in resources/public
  (ANY "*" [] echo)) ; Good for diagnostics

;;;  (-main :port 3034)
;;;  (web/stop (-main :port 3034))
(defn -main [& {:as args}]
  (web/run
    (-> routes
      (immutant/wrap-session {:timeout 20})
      (immutant/wrap-development)) ; POD added - saw it in the source, looked interesting.
    (merge {"host" (env :demo-web-host), "port" (env :demo-web-port)}
           args)))

;;; (Re)start the server
;(web/stop (-main :port 3034))
;(-main :port 3034)

;;;================== PROCESS =======================================================================


;;;================== JUNK ===========================================================================
          
#_(defn query-toplevel-chars
  []
  (sort
   (map '?/label
        (query am-kb '((?/subject rdfs/subPropertyOf top/curatedCharacteristic)
                       (?/subject rdfs/label ?/label))))))

#_(defn facility-search
  [request]
  (app-page-wrapper
   (html `[:select
           ~@(map (fn [x] `[:option {:value ~x} ~x]) ; POD doesn't like #(`...
                  (query-toplevel-chars))
           ])))
