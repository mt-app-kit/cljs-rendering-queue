
(ns rendering-queue.env
    (:require [rendering-queue.state :as state]
              [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-renderer-prop
  ; @ignore
  ;
  ; @description
  ; Returns a specific property of the renderer.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) prop-key
  ;
  ; @usage
  ; (get-renderer-prop :my-renderer :my-prop)
  ; =>
  ; "My value"
  ;
  ; @return (*)
  [renderer-id prop-key]
  (get-in @state/RENDERERS [renderer-id prop-key]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn renderer-inited?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the renderer is already initialized.
  ;
  ; @param (keyword) renderer-id
  ;
  ; @usage
  ; (renderer-inited? :my-renderer)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [renderer-id]
  (-> @state/RENDERERS (get renderer-id) map?))

(defn renderer-not-inited?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the renderer is not initialized.
  ;
  ; @param (keyword) renderer-id
  ;
  ; @usage
  ; (renderer-not-inited? :my-renderer)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [renderer-id]
  (-> @state/RENDERERS (get renderer-id) nil?))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-destroy-duration
  ; @ignore
  ;
  ; @description
  ; Returns how long does it take to destroy a specific amount of content.
  ;
  ; @param (keyword) renderer-id
  ; @param (integer) content-count
  ; Default: 1
  ;
  ; @usage
  ; (get-destroy-duration :my-renderer 1)
  ; =>
  ; 250
  ;
  ; @return (ms)
  ([renderer-id]
   (get-destroy-duration renderer-id 1))

  ([renderer-id content-count]
   (let [destroy-duration (get-renderer-prop renderer-id :destroy-duration)]
        (* destroy-duration content-count))))

(defn get-render-duration
  ; @ignore
  ;
  ; @description
  ; Returns how long does it take to render a specific amount of content.
  ;
  ; @param (keyword) renderer-id
  ; @param (integer) content-count
  ; Default: 1
  ;
  ; @usage
  ; (get-render-duration :my-renderer 1)
  ; =>
  ; 250
  ;
  ; @return (ms)
  ([renderer-id]
   (get-render-duration renderer-id 1))

  ([renderer-id content-count]
   (let [render-duration (get-renderer-prop renderer-id :render-duration)]
        (* render-duration content-count))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-first-content
  ; @ignore
  ;
  ; @description
  ; Returns the ID of the first content of the renderer.
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
  (let [rendered-contents (get-renderer-prop renderer-id :rendered-contents)]
       (first rendered-contents)))

(defn pushed-rendering-enabled?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the queue behavior of the renderer is ':push'.
  ;
  ; @param (keyword) renderer-id
  ;
  ; @usage
  ; (pushed-rendering-enabled? :my-renderer)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [renderer-id]
  (let [queue-behavior (get-renderer-prop renderer-id :queue-behavior)]
       (= queue-behavior :push)))

(defn queued-rendering-enabled?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the queue behavior of the renderer is ':wait'.
  ;
  ; @param (keyword) renderer-id
  ;
  ; @usage
  ; (queued-rendering-enabled? :my-renderer)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [renderer-id]
  (let [queue-behavior (get-renderer-prop renderer-id :queue-behavior)]
       (= queue-behavior :wait)))

(defn rerender-same-enabled?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if rerendering the same content by the renderer is enabled.
  ;
  ; @param (keyword) renderer-id
  ;
  ; @usage
  ; (rerender-same-enabled? :my-renderer)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [renderer-id]
  (get-renderer-prop renderer-id :rerender-same?))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn renderer-at-capacity?
  ; @description
  ; Returns TRUE if the maximum number of content of the renderer is reached.
  ;
  ; @param (keyword) renderer-id
  ;
  ; @usage
  ; (renderer-at-capacity? :my-renderer)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [renderer-id]
  (let [renderer-capacity (get-renderer-prop renderer-id :renderer-capacity)
        rendered-contents (get-renderer-prop renderer-id :rendered-contents)]
       (vector/item-count? rendered-components renderer-capacity)))

(defn renderer-not-at-capacity?
  ; @description
  ; Returns TRUE if the maximum number of content of the renderer is not reached.
  ;
  ; @param (keyword) renderer-id
  ;
  ; @usage
  ; (renderer-not-at-capacity? :my-renderer)
  ; =>
  ; true
  ;
  ; @return (boolean)
  [renderer-id]
  (let [renderer-at-capacity? (renderer-at-capacity? renderer-id)]
       (not renderer-at-capacity?)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn content-rendered?
  ; @description
  ; Returns TRUE if a specific content is rendered by the renderer.
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
  (let [rendered-contents (get-renderer-prop renderer-id :rendered-contents)]
       (vector/contains-item? rendered-contents content-id)))

(defn content-not-rendered?
  ; @description
  ; Returns TRUE if a specific content is not rendered by the renderer.
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
  (let [rendered-contents (get-renderer-prop renderer-id :rendered-contents)]
       (vector/not-contains-item? rendered-contents content-id)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-rendered-contents
  ; @description
  ; Returns the rendered contents of the renderer.
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
  (get-renderer-prop renderer-id :rendered-contents))
