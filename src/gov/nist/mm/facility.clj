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
            [edu.ucdenver.ccp.kr.jena.kb]
            [cheshire.core])
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
           {:process "Process", :product "Product", :equip "Equipment",
            :state "System State", :search "Search" :nb "Notebook"})])

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
  [obj & {:keys [root] :or {root "process/concept"}}]
  "Return a URL for an owl:Class."
  `[:a {:href ~(str root "?name=" (url-encode obj))} ~(str (name obj))])

#_(defn- process-pg
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

(defn- process-pg
  "Handle page for process tab."
  [request]
  (app-page-wrapper {:tab :process}
    (let [params (query mfg-kb '((?/x rdfs/subClassOf mfg/MachiningProcessParameter)))
          params-url (map owl-class-url (map '?/x params))]
      (html
        [:h1 "Process Characteristics"]
        `[:ul
          ~@(map (fn [d1] [:li d1]) params-url)]))))

(defn- product-pg
  "Handle page for product tab."
  [request]
  (app-page-wrapper {:tab :product}
   "Product: Nothing here yet."))

(defn- equipment-pg
  "Handle page for equipment tab."
  [request]
  (app-page-wrapper {:tab :equip}
     (let [mchars (query mfg-kb '((?/x rdfs/subClassOf mfg/MachineCharacteristic)))
           mchars-url (map owl-class-url (map '?/x mchars))]
       (html
        [:h1 "Equipment Characteristics"]
        `[:ul
          ~@(map (fn [d1] [:li d1]) mchars-url)]))))

(defn- state-pg
  "Handle page for facility state tab."
  [request]
  (app-page-wrapper {:tab :state}
   "State: Nothing here yet."))

(defn- search-pg
  "Handle page for search tab."
  [request]
  (app-page-wrapper {:tab :search}
                    "All: Nothing here yet."))

(defn- nb-pg
  "Handle page for notebook analysis."
  [request]
  (app-page-wrapper {:tab :nb}
   "Notebook: Nothing here yet."))

(defn page-tab
  "Return the keyword indicating the tab on which the page should be displayed."
  [request & {:keys [default] :or {default :process}}]
  (if-let [tab (:tab (:params request))]
    (keyword tab)
    default))

(defn- concept-desc-pg
  "Describe an owl:Class."
  [request]
  (app-page-wrapper {:tab (page-tab request)}
      (str "Got it: params = " (:params request) " query-string = " (url-decode (:query-string request)))))

(defroutes routes
  (GET "/" [] process-pg)
  (GET "/FacilitySearch/:tab" [tab] 
       (cond (= tab "process") process-pg
             (= tab "product") product-pg
             (= tab "equip"  ) equipment-pg
             (= tab "state"  ) state-pg
             (= tab "search" ) search-pg
             (= tab "nb"     ) nb-pg))
  (GET "/FacilitySearch/:tab/concept*" [tab] concept-desc-pg) 
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

;(def nb-content (cheshire.core/parse-stream
;                 (clojure.java.io/reader "resources/TurningOptimization.ipynb")
;                 true))

(def nb-test  {:cell_type "markdown",
               :metadata {},
               :source
               ["| Symbol | Variable | Meaning |\n"
                "|-------------------------------------------------------|\n"
                "| $D_{LSL}$ | D_LSL | Lower bound on final part diameter|\n"
                "| $D_{USL}$ | D_USL |Upper bound on final part diameter|\n"
                "| $R_{USL}$ | R_USL | Maximum allowable surface roughness |\n"
                "\n"
                "The paper provides the following values:"]})

;;; POD rewrite with reduce
(defn- table-vals
  [text]
  "Returns vector of table rows (list of the row's elements) if TEXT (vector of strings) start a table."
  (when (re-matches #"^\s*\|[-,:,\|]+\|.*\n$" (text 0)) ; second line of markdown table is just |------|
    (let [len (dec (count text))]
      (loop [line-num 1
             rows []] ; odd-looking because only want first table in this cell (if many).
        (if (and (< line-num len) 
                 (re-matches #"\s*\|.*\|\n$" (text line-num)))
          (recur
           (inc line-num)
           (conj rows (butlast (rest (str/split (text line-num) #"\|")))))
          rows)))))

(defn- find-tables
  "Return those tables from markdown cells of the file that look like they define variables/parameters."
  [content]
  (reduce (fn [doc-tabs cell]
            (let [text (:source cell)
                  res
                  (reduce (fn [cell-tabs line]
                            (if (re-matches
                                 #"^\s*\|\s*([S,s]ymbol|[P,p]arameter|[V,v]ariable)\s*\|.*\n$"
                                 line)
                              (concat cell-tabs (table-vals (subvec text (inc (.indexOf text line)))))
                              cell-tabs))
                          [] ; cell-tables (tables in this cell)
                          text)]
              (if (empty? res) doc-tabs (conj doc-tabs res))))
          [] ; document-tables (tables in whole document)
          (filter #(= (:cell_type %) "markdown") (:cells content))))

#_(defn- find-tables
  "Return those tables from markdown cells of the file that look like they define variables/parameters."
  [content]
  (persistent!
   (reduce (fn [doc-tabs cell]
             (let [text (:source cell)
                   res
                   (persistent!
                    (reduce (fn [cell-tabs line]
                              (if (re-matches
                                   #"^\s*\|\s* ([S,s]ymbol | [P,p]arameter| [V,v]ariable)\s*\|.*\n$"
                                   line)
                                (conj! cell-tabs (table-vals (subvec text (inc (.indexOf text line)))))
                                cell-tabs))
                            (transient []) ; cell-tables (tables in this cell)
                            text))]
               (if (empty? res) doc-tabs (conj! doc-tabs res))))
           (transient []) ; document-table (tables in whole document)
           (filter #(= (:cell_type %) "markdown") (:cells content)))))

            

                        
         
;            len (dec (count text))]
;        (for [line-num 0
;               tabs tables]
;          (if (< line-num len)
;            (if (re-matches
;                 #"^\s*\|\s* ([S,s]ymbol | [P,p]arameter| [V,v]ariable)\s*\|.*\n$"
;                 (line-num text))
;              (recur (inc line-num) (conj! (table-vals



#_(defn facility-search
  [request]
  (app-page-wrapper
   (html `[:select
           ~@(map (fn [x] `[:option {:value ~x} ~x]) ; POD doesn't like #(`...
                  (query-toplevel-chars))
           ])))
