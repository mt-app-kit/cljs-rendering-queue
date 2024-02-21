
(ns rendering-queue.side-effects
    (:require [rendering-queue.state :as state]
              [rendering-queue.env :as env]
              [rendering-queue.prototypes :as prototypes]
              [fruits.vector.api :as vector]
              [time.api :as time]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn queue-task!
  ; @ignore
  ;
  ; @description
  ; Adds the given function to the task queue of the renderer.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) content-id
  ; @param (function) task-f
  ;
  ; @usage
  ; (queue-task! :my-renderer :my-content request-rendering!)
  [renderer-id content-id task-f]
  (swap! state/RENDERERS update-in [renderer-id :task-queue] vector/conj-item [renderer-id content-id task-f]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn log-render-event!
  ; @ignore
  ;
  ; @description
  ; Adds the given event key to the render log of the content.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) content-id
  ; @param (keyword) event-key
  ;
  ; @usage
  ; (log-render-event! :my-renderer :my-content :my-event)
  [renderer-id content-id event-key]
  (let [timestamp (time/elapsed)]
       (swap! state/RENDERERS update-in [renderer-id :render-log content-id event-key] vector/conj-item timestamp)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn init-renderer!
  ; @description
  ; Stores the given properties of the renderer (in the 'RENDERERS' atom)
  ;
  ; @param (keyword) renderer-id
  ; @param (map) renderer-props
  ; {:destroy-duration (ms)(opt)
  ;   Default: 0
  ;  :queue-behavior (keyword)(opt)
  ;   :ignore, :push, :wait
  ;   Default: :ignore
  ;  :render-duration (ms)(opt)
  ;   Default: 0
  ;  :renderer-capacity (integer)(opt)
  ;   Default: 1
  ;  :rerender-same? (boolean)(opt)
  ;   Default: false}
  ;
  ; @usage
  ; (init-renderer! :my-renderer {...})
  [renderer-id renderer-props]
  (let [renderer-props (prototypes/renderer-props-prototype renderer-id renderer-props)]
       (swap! state/RENDERERS assoc renderer-id renderer-props)))

(defn update-renderer!
  ; @description
  ; Merges the given properties of the renderer onto its stored properties (in the 'RENDERERS' atom)
  ;
  ; @param (keyword) renderer-id
  ; @param (map) renderer-props
  ; {:destroy-duration (ms)(opt)
  ;  :queue-behavior (keyword)(opt)
  ;   :ignore, :push, :wait
  ;  :render-duration (ms)(opt)
  ;  :renderer-capacity (integer)(opt)
  ;  :rerender-same? (boolean)(opt)}
  ;
  ; @usage
  ; (update-renderer! :my-renderer {...})
  [renderer-id renderer-props]
  (swap! state/RENDERERS merge renderer-id renderer-props))

(defn destruct-renderer!
  ; @description
  ; Clears the properties and rendered contents of the renderer (from the 'RENDERERS' atom)
  ;
  ; @param (keyword) renderer-id
  ;
  ; @usage
  ; (destruct-renderer! :my-renderer)
  [renderer-id]
  (swap! state/RENDERERS dissoc renderer-id))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn reserve-renderer!
  ; @ignore
  ;
  ; @description
  ; Reserves the renderer (optionally delayed).
  ;
  ; @param (keyword) renderer-id
  ; @param (ms)(opt) delay
  ; Default: 0
  ;
  ; @usage
  ; (reserve-renderer! :my-renderer)
  ([renderer-id]
   (reserve-renderer! renderer-id 0))

  ([renderer-id delay]
   (letfn [(f0 [] (swap! state/RENDERERS assoc-in [renderer-id :reserved?] true))]
          (time/set-timeout! delay f0))))

(defn free-renderer!
  ; @ignore
  ;
  ; @description
  ; Frees the renderer (optionally delayed).
  ;
  ; @param (keyword) renderer-id
  ; @param (ms)(opt) delay
  ; Default: 0
  ;
  ; @usage
  ; (free-renderer! :my-renderer)
  ([renderer-id]
   (free-renderer! renderer-id 0))

  ([renderer-id delay]
   (letfn [(f0 [] (swap! state/RENDERERS assoc-in [renderer-id :reserved?] false))]
          (time/set-timeout! delay f0))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn destroy-content!
  ; @ignore
  ;
  ; @description
  ; Destroys a specific content of the renderer.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) content-id
  ;
  ; @usage
  ; (destroy-content! :my-renderer :my-content)
  [renderer-id content-id]
  (reserve-renderer! renderer-id)
  (log-render-event! renderer-id content-id :destroyed-at)
  (swap! state/RENDERERS update-in [renderer-id :rendered-contents] vector/remove-item content-id)
  (let [destroy-duration (env/get-destroy-duration renderer-id)]
       (free-renderer! renderer-id destroy-duration)))

(defn ignore-destroying!
  ; @ignore
  ;
  ; @description
  ; Ignores destroying a specific content of the renderer.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) content-id
  ;
  ; @usage
  ; (ignore-destroying! :my-renderer :my-content)
  [renderer-id content-id]
  (log-render-event! renderer-id content-id :destroy-ignored-at))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn request-destroying!
  ; @description
  ; Initiates destroying a specific content rendered by the renderer.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) content-id
  ;
  ; @usage
  ; (destroy-content! :my-renderer :my-content)
  [renderer-id content-id]
  (if (env/content-not-rendered? renderer-id content-id)
      (ignore-destroying!        renderer-id content-id)
      (cond (env/renderer-reserved? renderer-id) (queue-task!      renderer-id content-id request-destroying!)
            :destroy-content                     (destroy-content! renderer-id content-id))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn render-content!
  ; @ignore
  ;
  ; @description
  ; Renders the given content with the renderer.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) content-id
  ;
  ; @usage
  ; (render-content! :my-renderer :my-content)
  [renderer-id content-id]
  (reserve-renderer! renderer-id)
  (log-render-event! renderer-id content-id :rendered-at)
  (swap! state/RENDERERS update-in [renderer-id :rendered-contents] vector/conj-item content-id)
  (let [render-duration (env/get-render-duration renderer-id)]
       (free-renderer! renderer-id render-duration)))

(defn rerender-content!
  ; @ignore
  ;
  ; @description
  ; Rerenders the given content with the renderer.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) content-id
  ;
  ; @usage
  ; (rerender-content! :my-renderer :my-content)
  [renderer-id content-id]
  (reserve-renderer! renderer-id)
  (log-render-event! renderer-id content-id :rerendered-at)
  (queue-task!       renderer-id content-id render-content!)
  (destroy-content!  renderer-id content-id))

(defn push-content!
  ; @ignore
  ;
  ; @description
  ; Destroys the first content of the renderer, then renders the given content.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) content-id
  ;
  ; @usage
  ; (push-content! :my-renderer :my-content)
  [renderer-id content-id]
  (reserve-renderer!      renderer-id)
  (log-render-event!      renderer-id content-id :pushed-at)
  (queue-task!            renderer-id content-id render-content!)
  (destroy-first-content! renderer-id))

(defn ignore-rendering!
  ; @ignore
  ;
  ; @description
  ; Ignores rendering the given content with the renderer.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) content-id
  ;
  ; @usage
  ; (ignore-rendering! :my-renderer :my-content)
  [renderer-id content-id]
  (log-render-event! renderer-id content-id :render-ignored-at))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn request-rendering!
  ; @description
  ; Initiates rendering the given content with the renderer.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) content-id
  ;
  ; @usage
  ; (destroy-rendering! :my-renderer :my-content)
  [renderer-id content-id]
  (if (env/content-rendered? renderer-id content-id)
      (cond (env/renderer-reserved?        renderer-id) (queue-task!       renderer-id content-id request-rendering!)
            (env/rerender-same-enabled?    renderer-id) (rerender-content! renderer-id content-id)
            :ignore-rendering                           (ignore-rendering! renderer-id content-id))
      (cond (env/renderer-reserved?        renderer-id) (queue-task!       renderer-id content-id request-rendering!)
            (env/renderer-not-at-capacity? renderer-id) (render-content!   renderer-id content-id)
            (env/pushed-rendering-enabled? renderer-id) (push-content!     renderer-id content-id)
            (env/queued-rendering-enabled? renderer-id) (queue-task!       renderer-id content-id request-rendering!)
            :ignore-rendering                           (ignore-rendering! renderer-id content-id))))
