(ns core.async.impl.ioc-alt
  (:require [core.async.impl.ioc-macros :refer :all :as m]
            [core.async.impl.protocols :as impl]))

(defrecord Park [ids cont-block]
  IInstruction
  (reads-from [this] ids)
  (writes-to [this] [])
  (block-references [this] [])
  (emit-instruction [this state-sym]
    (let [[ports opts] ids]
      `(core.async/do-alts (fn [val#]
                             (m/async-chan-wrapper (assoc ~state-sym ::m/value val# ::m/state ~cont-block)))
                           ~ports
                           ~opts))))


(defmethod sexpr-to-ssa 'core.async.impl.ioc-alt/alts!
  [[_ & args]]
  (gen-plan
   [ids (all (map item-to-ssa args))
    cont-block (add-block)
    park-id (add-instruction (->Park ids cont-block))
    _ (set-block cont-block)
    ret-id (add-instruction (->Const ::m/value))]
   ret-id))

