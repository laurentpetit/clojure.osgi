(ns clojure.osgi.core
  (:refer-clojure :exlude [*pending-paths*]) 
)

(def ^:dynamic *bundle*)

; copy from clojure.core BEGIN
(defn- root-resource
  "Returns the root directory path for a lib"
  {:tag String}
  [lib]
  (str \/
       (.. (name lib)
           (replace \- \_)
           (replace \. \/))))

(defn- root-directory
  "Returns the root resource path for a lib"
  [lib]
  (let [d (root-resource lib)]
    (subs d 0 (.lastIndexOf d "/"))))


(defonce
  ^{:private true
    :dynamic true
    :doc "the set of paths currently being loaded by this thread"}
  *pending-paths* #{})

; copy from clojure.core - END

(defn osgi-load [path]
  (let [^String path (if (.startsWith path "/")
                       path
                       (str (root-directory (ns-name *ns*)) \/ path))]
    
    (if-not (*pending-paths* path)
      (do
        (binding [*pending-paths* (conj *pending-paths* path)]
          (clojure.osgi.ClojureOSGi/load (.substring path 1) *bundle*)
          )
        )
	  )
  )
)

(alter-var-root (find-var (symbol "clojure.core" "load")) 
 (fn [original]
   (fn [path] (if (bound? #'*bundle*)
                (osgi-load path)
                (original path)))))

;(alter-var-root (find-var (symbol "clojure.core" "load")) 
; (fn [original]
;   (fn [path]
;		  (when osgi-debug
;		    (println (str "load " path " from " (.getSymbolicName *bundle*))))
;		 
;		  (let [path (full-path path)]
;			  (if-not (*pending-paths* path)
;				  (binding [
;		                *pending-paths* (conj *pending-paths* path)
;		                *currently-loading* path]
;		        (let [load (fn [] (clojure.lang.RT/load (.substring path 1)))]
;						  (if-let [bundle (bundle-for-resource *bundle* (str path ".clj"))]
;		            (with-bundle* bundle load)					    
;						    (load))
;		        )
;		 	    )
;			  )
;		  )
;   )
; ) 
;)


(defn osgi-require [name]
 (require name)
)

(defn bundle-name []
  (.getSymbolicName *bundle*)
) 




