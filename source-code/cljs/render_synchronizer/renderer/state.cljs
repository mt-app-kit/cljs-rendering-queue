
(ns render-synchronizer.renderer.state
    (:require [reagent.core :as reagent]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @description
; Stored properties and rendered contents of renderers.
;
; @atom (map)
; {:my-renderer (map)
;   {:destroy-duration (ms)
;    :queue-behavior (keyword)
;    :render-duration (ms)
;    :render-log (map)
;    :rendered-contents (keywords in vector)
;    :renderer-capacity (integer)
;    :rerender-same? (boolean)
;    :reserved-for (keyword)
;    :task-queue (vectors in vector)
;     [(keyword) renderer-id
;      (keyword) content-id
;      (keyword) task-id
;      (function) task-f]}}
;
; @usage
; (deref RENDERERS)
; =>
; {:my-renderer {:destroy-duration  250
;                :queue-behavior    :push
;                :render-duration   250
;                :render-log        {:my-content {:rendered-at ["..."]}}
;                :rendered-contents [:my-content]
;                :renderer-capacity 256
;                :rerender-same?    true
;                :task-queue        []}}
(def RENDERERS (reagent/atom {}))
