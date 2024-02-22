
(ns rendering-queue.content.env
    (:require [rendering-queue.renderer.env :as renderer.env]
              [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-rendered-contents
  ; @description
  ; Returns the list of rendered contents of the renderer.
  ;
  ; @param (keyword) renderer-id
  ;
  ; @usage
  ; (get-rendered-contents :my-renderer)
  ; =>
  ; [:my-content]
  ;
  ; @return (keywords in vector)
  [renderer-id]
  (renderer.env/get-renderer-state renderer-id :rendered-contents))

(defn get-first-content
  ; @description
  ; Returns the first ID from the list of rendered contents of the renderer.
  ;
  ; @param (keyword) renderer-id
  ;
  ; @usage
  ; (get-first-content :my-renderer)
  ; =>
  ; :my-content
  ;
  ; @return (keyword)
  [renderer-id]
  (let [rendered-contents (get-rendered-contents renderer-id)]
       (first rendered-contents)))
 
;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn content-rendered?
  ; @description
  ; Returns TRUE if the list of rendered contents of the renderer contains the given content ID.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) content-id
  ;
  ; @usage
  ; (content-rendered? :my-renderer :my-content)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [renderer-id content-id]
  (let [rendered-contents (get-rendered-contents renderer-id)]
       (vector/contains-item? rendered-contents content-id)))

(defn content-not-rendered?
  ; @description
  ; Returns TRUE if the list of rendered contents of the renderer doesn't contain the given content ID.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) content-id
  ;
  ; @usage
  ; (content-not-rendered? :my-renderer :my-content)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [renderer-id content-id]
  (let [rendered-contents (get-rendered-contents renderer-id)]
       (vector/not-contains-item? rendered-contents content-id)))
