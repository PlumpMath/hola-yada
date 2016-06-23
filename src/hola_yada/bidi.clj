(ns hola-yada.bidi
  (:require [bidi.bidi :as b]
            [bidi.ring :refer (make-handler)]
            [ring.util.response :as res]))

 

;; A route that matches /index.html
(def route ["/index.html" :index])

;; To check the match
(b/match-route route "/index.html")
;;=> {:handler :index}

;; A map is returned with a :handler key and a value, in this case a keyword, which we
;; could use to look up  a handler in a map.

;; If we try to match an undefined route, nil is returned.
(b/match-route route "/about.html")
;;=> nil

;; Now in the other direction
(b/path-for route :index)
;;=> "/index.html"

;; ***** Multiples routes ********
(def my-routes ["/" {"index.html" :index
                     "about.html" :about}])

(b/match-route my-routes "/about.html")
;;=> {:handler :about}
(b/path-for my-routes :about)
;;=> "/about.html"

(def more-routes ["/" {"index.html" :index
                       "about.html" :about
                       "articles/" {"index.html" :article-index
                                    "articles.html" :articles}}])

(b/match-route more-routes "/articles/index.html")
;;=> {:handler :article-index}

;; ************ Route patterns *********
;; The pattern is constructed in segments using a clojure vector
(def more-routes-id ["/" {"index.html" :index
                          "about.html" :about
                          "articles/" {"index.html" :article-index
                                       [:id "/article.html"] :articles}}])

(b/match-route more-routes-id "/articles/123/article.html")
;;=> {:route-params {:id "123"}, :handler :articles}
;; Now we get the handler that matches with this path and the route-params. 

(b/path-for more-routes-id :article-index)
;;=> "/articles/index.html"

;; In order to get the path that matches with the :articles handler, we need to
;; add the :id parameter

(b/path-for more-routes-id :articles :id 999)
;;=> "/articles/999/article.html"

;; ********* Wrapping as a Ring handler ***********

;; Match results can be any value, but are typically functions (either in-line or via a 
;; symbol reference). You can easily wrap your routes to form a Ring handler (similar to
;;  what Compojure's routes and defroutes does) with the make-handler function.

(defn index-handler
  [request]
  res/response "Homepage")

(defn article-handler
  [{:keys [route-params]}]
  (res/response (str "You are viewing the article " (:id route-params))))

(def handler
  (make-handler ["/" {"index.html" :index
                      "about.html" :about
                      "articles/" {"index.html" index-handler
                                   [:id "/article.html"] article-handler}}]))

;; ******** Guards **************
;; By default, routes ignore the request method, behaving like Compojure's ANY routes.
;; if you want to limit a route to a request method, you can wrap the route in a pair  
;; (or map entry), using a keyword for the pattern. The keyword denotes the request
;;  method (:get, :put, etc.)

["/" {"blog" {:get {"/index" (fn [req] {:status 200 :body "Index"})}}}]

;; You can also restrict routes by any other request criteria. Guards are specified by
;; maps. Map entries can specify a single value, a set of possible values or even a
;; predicate to test a value.

;; In this example, the /zip route is only matched if the server name in the request
;; is juxt.pro. You can use this feature to restrict routes to virtual hosts or
;; HTTP schemes.

["/" {"blog" {:get
                {"/index" (fn [req] {:status 200 :body "Index"})}}
              {:request-method :post :server-name "juxt.pro"}
                {"/zip" (fn [req] {:status 201 :body "Created"})}}]

;; ************* Keywords  ************

(def ruta [["foo/" [ keyword :db/ident ] "/bar" ] :foo-handler])

(b/path-for ruta :foo-handler :db/ident :hola)
;;=>  "foo/hola/bar"

;; ************** Catch-All Routes ***********

(def mis-rutas ["/" [["index.html" :index]
                     [true         :not-found]]])

;; We used vectors rather than maps to define the routes because the order of the
;; definitions is significant (i.e. true will completely subsume the other routes
;; if we let it).

;; Now let's try to match on that:

user> (b/match-route my-routes "/index.html")
;;=> {:handler :index}
user> (b/match-route my-routes "/other.html")
;;=> {:handler :not-found}

;; ***************** Route definitions **************

;; A route is formed as a pair: [ <pattern> <matched> ]

;; The left-hand-side of a pair is the pattern. It can match a path, either fully or 
;; partially. The simplest pattern is a string, but other types of patterns are also 
;; possible, including segmented paths, regular expressions, records, in various
;; combinations.

;; The right-hand-side indicates the result of the match (in the case that the
;; pattern is matched fully) or a route sub-structure that attempts to match 
;; on the remainder of the path (in the case that the pattern is matched partially). 
;; The route structure is a recursive structure.

;; This BNF grammar formally defines the basic route structure.

;; RouteStructure := RoutePair

;; RoutePair ::= [ Pattern Matched ]

;; Pattern ::= Path | [ PatternSegment+ ] | MethodGuard | GeneralGuard | true | false

;; MethodGuard ::= :get :post :put :delete :head :options

;; GeneralGuard ::= [ GuardKey GuardValue ]* (a map)

;; GuardKey ::= Keyword

;; GuardValue ::= Value | Set | Function

;; Path ::= String

;; PatternSegment ::= String | Regex | Keyword | [ (String | Regex) Keyword ]

;; Matched ::= Function | Symbol | Keyword | [ RoutePair+ ] { RoutePair+ }

;; In case of confusion, refer to bidi examples found in this README and in the test suite.

;; A schema is available as bidi.schema/RoutePair. You can use this to check or validate a bidi route structure in your code.

(require '[schema.core :as s] bidi.schema)

(def route ["/index.html" :index])

;; Check that the route is properly structured - returns nil if valid;
;; otherwise, returns a value with 'bad' parts of the route.
(s/check bidi.schema/RoutePair route)

;; Throw an exception if the route is badly structured
(s/validate bidi.schema/RoutePair route)
