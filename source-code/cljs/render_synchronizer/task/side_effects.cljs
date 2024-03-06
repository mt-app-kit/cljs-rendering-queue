
(ns render-synchronizer.task.side-effects
    (:require [fruits.map.api                     :refer [dissoc-in]]
              [fruits.vector.api                  :as vector]
              [render-synchronizer.content.env    :as content.env]
              [render-synchronizer.renderer.env   :as renderer.env]
              [render-synchronizer.renderer.state :as renderer.state]
              [render-synchronizer.task.env       :as task.env]
              [time.api                           :as time]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn error-catched
  ; @ignore
  ;
  ; @description
  ; Throws an error with the given error ID.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) error-id
  ; @param (keyword) task-key
  ;
  ; @usage
  ; (error-catched :my-renderer :my-error :my-task)
  [renderer-id error-id task-key]
  (let [error-message (str error-id " error catched in renderer: " renderer-id " while executing task: " task-key)]
       (throw (js/Error. error-message))))

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
  ; @param (keyword) task-id
  ; @param (keyword) event-key
  ;
  ; @usage
  ; (log-render-event! :my-renderer :my-content :xxxx :my-event)
  [renderer-id content-id _ event-key]
  (let [timestamp (time/elapsed)]
       (swap! renderer.state/RENDERERS update-in [renderer-id :render-log content-id event-key] vector/conj-item timestamp)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn reserve-renderer!
  ; @ignore
  ;
  ; @description
  ; Reserves the renderer for a specific task (optionally delayed).
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) content-id
  ; @param (keyword) task-id
  ; @param (ms)(opt) delay
  ; Default: 0
  ;
  ; @usage
  ; (reserve-renderer! :my-renderer :my-content :xxxx)
  ([renderer-id content-id task-id]
   (reserve-renderer! renderer-id content-id task-id 0))

  ([renderer-id _ task-id delay]
   (letfn [(f0 [] (swap! renderer.state/RENDERERS assoc-in [renderer-id :reserved-for] task-id))]
          (time/set-timeout! f0 delay))))

(defn free-up-renderer!
  ; @ignore
  ;
  ; @description
  ; Frees up the renderer (optionally delayed).
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) content-id
  ; @param (keyword) task-id
  ; @param (ms)(opt) delay
  ; Default: 0
  ;
  ; @usage
  ; (free-up-renderer! :my-renderer :my-content :xxxx)
  ([renderer-id content-id task-id]
   (free-up-renderer! renderer-id content-id task-id 0))

  ([renderer-id _ _ delay]
   (letfn [(f0 [] (swap! renderer.state/RENDERERS dissoc-in [renderer-id :reserved-for]))]
          (time/set-timeout! f0 delay))))

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
  ; @param (keyword) task-id
  ; @param (function) task-f
  ;
  ; @usage
  ; (queue-task! :my-renderer :my-content :xxxx request-rendering!)
  [renderer-id content-id task-id task-f]
  (swap! renderer.state/RENDERERS update-in [renderer-id :task-queue] vector/conj-item [renderer-id content-id task-id task-f]))

(defn do-task-from-queue!
  ; @ignore
  ;
  ; @description
  ; Applies the next function from the task queue of the renderer.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) content-id
  ; @param (keyword) task-id
  ; @param (ms)(opt) delay
  ; Default: 0
  ;
  ; @usage
  ; (do-task-from-queue! :my-renderer :my-content :xxxx request-rendering!)
  ([renderer-id content-id task-id]
   (do-task-from-queue! renderer-id content-id task-id 0))

  ([renderer-id _ task-id delay]
   (letfn [(f0 [] (when-let [[_ content-id _ task-f] (task.env/get-next-task renderer-id task-id)]
                            (swap! renderer.state/RENDERERS update-in [renderer-id :task-queue] vector/remove-first-item)
                            (task-f renderer-id content-id task-id)))]
          (time/set-timeout! f0 delay))))

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
  ; @param (keyword) task-id
  ;
  ; @usage
  ; (destroy-content! :my-renderer :my-content :xxxx)
  [renderer-id content-id task-id]
  (reserve-renderer! renderer-id content-id task-id)
  (log-render-event! renderer-id content-id task-id :destroyed-at)
  (swap! renderer.state/RENDERERS update-in [renderer-id :rendered-contents] vector/remove-item content-id)
  (let [destroy-duration (task.env/get-destroy-duration renderer-id task-id)]
       (if (task.env/any-task-to-do? renderer-id task-id)
           (do-task-from-queue!      renderer-id content-id task-id destroy-duration)
           (free-up-renderer!        renderer-id content-id task-id destroy-duration))))

(defn ignore-destroying!
  ; @ignore
  ;
  ; @description
  ; Ignores destroying a specific content of the renderer.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) content-id
  ; @param (keyword) task-id
  ;
  ; @usage
  ; (ignore-destroying! :my-renderer :my-content :xxxx)
  [renderer-id content-id task-id]
  (log-render-event! renderer-id content-id task-id :destroy-ignored-at))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn select-destroying-method!
  ; @ignore
  ;
  ; @description
  ; Selects destroying method.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) content-id
  ; @param (keyword) task-id
  ;
  ; @usage
  ; (select-destroying-method! :my-renderer :my-content :xxxx)
  [renderer-id content-id task-id]
  (if (content.env/content-rendered? renderer-id content-id)
      (cond (task.env/renderer-not-inited? renderer-id task-id) (error-catched      renderer-id :uninitialized-renderer :request-destroying!)
            (task.env/renderer-reserved?   renderer-id task-id) (queue-task!        renderer-id content-id task-id #'select-destroying-method!)
            :destroy-content                                    (destroy-content!   renderer-id content-id task-id))
      (cond (task.env/renderer-not-inited? renderer-id task-id) (error-catched      renderer-id :uninitialized-renderer :request-destroying!)
            :ignore-destroying                                  (ignore-destroying! renderer-id content-id task-id))))

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
  ; @param (keyword) task-id
  ;
  ; @usage
  ; (render-content! :my-renderer :my-content :xxxx)
  [renderer-id content-id task-id]
  (reserve-renderer! renderer-id content-id task-id)
  (log-render-event! renderer-id content-id task-id :rendered-at)
  (swap! renderer.state/RENDERERS update-in [renderer-id :rendered-contents] vector/conj-item content-id)
  (let [render-duration (task.env/get-render-duration renderer-id task-id)]
       (if (task.env/any-task-to-do? renderer-id task-id)
           (do-task-from-queue! renderer-id content-id task-id render-duration)
           (free-up-renderer!   renderer-id content-id task-id render-duration))))

(defn rerender-content!
  ; @ignore
  ;
  ; @description
  ; Rerenders the given content with the renderer.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) content-id
  ; @param (keyword) task-id
  ;
  ; @usage
  ; (rerender-content! :my-renderer :my-content :xxxx)
  [renderer-id content-id task-id]
  (reserve-renderer! renderer-id content-id task-id)
  (log-render-event! renderer-id content-id task-id :rerendered-at)
  (queue-task!       renderer-id content-id task-id render-content!)
  (destroy-content!  renderer-id content-id task-id))

(defn push-content!
  ; @ignore
  ;
  ; @description
  ; Destroys the first content of the renderer, then renders the given content.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) content-id
  ; @param (keyword) task-id
  ;
  ; @usage
  ; (push-content! :my-renderer :my-content :xxxx)
  [renderer-id content-id task-id]
  (reserve-renderer! renderer-id content-id task-id)
  (log-render-event! renderer-id content-id task-id :pushed-at)
  (queue-task!       renderer-id content-id task-id render-content!)
  (let [first-content-id (content.env/get-first-content renderer-id)]
       (destroy-content! renderer-id first-content-id task-id)))

(defn ignore-rendering!
  ; @ignore
  ;
  ; @description
  ; Ignores rendering the given content with the renderer.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) content-id
  ; @param (keyword) task-id
  ;
  ; @usage
  ; (ignore-rendering! :my-renderer :my-content :xxxx)
  [renderer-id content-id task-id]
  (log-render-event! renderer-id content-id task-id :render-ignored-at))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn select-rendering-method!
  ; @ignore
  ;
  ; @description
  ; Selects rendering method.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword) content-id
  ; @param (keyword) task-id
  ;
  ; @usage
  ; (select-rendering-method! :my-renderer :my-content :xxxx)
  [renderer-id content-id task-id]
  (if (content.env/content-rendered? renderer-id content-id)
      (cond (task.env/renderer-not-inited?      renderer-id task-id) (error-catched     renderer-id :uninitialized-renderer :request-rendering!)
            (task.env/renderer-reserved?        renderer-id task-id) (queue-task!       renderer-id content-id task-id #'select-rendering-method!)
            (task.env/rerender-same-enabled?    renderer-id task-id) (rerender-content! renderer-id content-id task-id)
            :ignore-rendering                                        (ignore-rendering! renderer-id content-id task-id))
      (cond (task.env/renderer-not-inited?      renderer-id task-id) (error-catched     renderer-id :uninitialized-renderer :request-rendering!)
            (task.env/renderer-reserved?        renderer-id task-id) (queue-task!       renderer-id content-id task-id #'select-rendering-method!)
            (task.env/renderer-not-at-capacity? renderer-id task-id) (render-content!   renderer-id content-id task-id)
            (task.env/pushed-rendering-enabled? renderer-id task-id) (push-content!     renderer-id content-id task-id)
            (task.env/queued-rendering-enabled? renderer-id task-id) (queue-task!       renderer-id content-id task-id #'select-rendering-method!)
            :ignore-rendering                                        (ignore-rendering! renderer-id content-id task-id))))
