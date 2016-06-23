(ns hola-yada.yada
  (:require [yada.yada :refer [yada as-resource server handler routes resource]]
            [aleph.http :refer [start-server]]
            [bidi.ring :refer [make-handler] :as bidi]
            ))

         
        
(start-server
  (yada "Hello World!\n")
  {:port 3000})



