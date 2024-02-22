
(ns render-synchronizer.renderer.env
    (:require [render-synchronizer.renderer.state :as renderer.state]
              [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn get-renderer-state
  ; @ignore
  ;
  ; @description
  ; Returns the state of the renderer optionally filtered to a specific property.
  ;
  ; @param (keyword) renderer-id
  ; @param (keyword)(opt) prop-key
  ;
  ; @usage
  ; (get-renderer-state :my-renderer)
  ; =>
  ; {...}
  ;
  ; @usage
  ; (get-renderer-state :my-renderer :my-prop)
  ; =>
  ; "My value"
  ;
  ; @return (*)
  [renderer-id & [prop-key]]
  (if prop-key (get-in @renderer.state/RENDERERS [renderer-id prop-key])
               (get-in @renderer.state/RENDERERS [renderer-id])))
