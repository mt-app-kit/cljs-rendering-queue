
(ns render-synchronizer.renderer.side-effects
    (:require [render-synchronizer.renderer.prototypes :as renderer.prototypes]
              [render-synchronizer.renderer.state      :as renderer.state]))

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
  (let [renderer-props (renderer.prototypes/renderer-props-prototype renderer-id renderer-props)]
       (swap! renderer.state/RENDERERS assoc renderer-id renderer-props)))

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
  (swap! renderer.state/RENDERERS merge renderer-id renderer-props))

(defn destruct-renderer!
  ; @description
  ; Clears the properties and the rendered contents of the renderer (from the 'RENDERERS' atom)
  ;
  ; @param (keyword) renderer-id
  ;
  ; @usage
  ; (destruct-renderer! :my-renderer)
  [renderer-id]
  (swap! renderer.state/RENDERERS dissoc renderer-id))
