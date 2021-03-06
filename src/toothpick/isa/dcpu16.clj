(ns toothpick.dcpu16
  (:require [toothpick.core :refer [define-architecture
                                    op reg bit-fmt
                                    assemble-label
                                    assemble-align
                                    assemble-padding]]))

;; An assembler for Notch's fictional DCPU16 as documented here:
;;     http://dcpu.com/dcpu-16/

;; define architectural registers

(def dopfmt [6 5 5])


(define-architecture dcpu16
  ;; general purpose regist ers...
  ;;------------------------------------------------------------------------
  (reg 0x00 "A")
  (reg 0x01 "B")
  (reg 0x02 "C")
  (reg 0x03 "X")
  (reg 0x04 "Y")
  (reg 0x05 "Z")
  (reg 0x06 "I")
  (reg 0x07 "J")

  ;; special purpose registers...
  ;;------------------------------------------------------------------------
  (reg 0x1b "SB")
  (reg 0x1c "PC")
  (reg 0x1d "EX")

  ;; opcodes...
  ;;------------------------------------------------------------------------
  (op 0x01 "SET" dopfmt
      "sets b to a")

  (op 0x02 "ADD" dopfmt
      "sets b to b + a, sets EX to 0x0001 on overflow")

  (op 0x03 "SUB" dopfmt
      "sets b to b - a, sets EX to 0xFFFF on underflow")

  (op 0x04 "MUL" dopfmt
      "sets b to b*a, sets EX to ((b*a)>>16)&0xffff)."
      " (treats b, a as unsigned)")

  (op 0x05 "MLI" dopfmt
      "like MUL, but treat b, a as signed")

  (op 0x06 "DIV" dopfmt
      "sets b to b/a, sets EX to ((b<<16)/a)&0xffff."
      " if a==0, sets b and EX to 0 instead. (treats b, a as unsigned)")

  (op 0x07 "DVI" dopfmt
      "like DIV, but treat b, a as signed. Rounds towards 0")

  (op 0x08 "MOD" dopfmt
      " sets b to b%a. if a==0, sets b to 0 instead.")

  (op 0x09 "MDI" dopfmt
      "like MOD, but treat b, a as signed. (MDI -7, 16 == -7)")

  (op 0x0a "AND" dopfmt
      "sets b to b&a")

  (op 0x0b "BOR" dopfmt
      "sets b to b|a")

  (op 0x0c "XOR" dopfmt
      "sets b to b^a")

  (op 0x0d "SHR" dopfmt
      "sets b to b>>>a, sets EX to ((b<<16)>>a)&0xffff"
      " (logical shift)")

  (op 0x0e "ASR" dopfmt
      "sets b to b>>a, sets EX to ((b<<16)>>>a)&0xffff"
      " (arithmetic shift) (treats b as signed)")

  (op 0x0f "SHL" dopfmt
      "sets b to b<<a, sets EX to ((b<<a)>>16)&0xffff")

  (op 0x10 "IFB" dopfmt
      "performs next instruction only if (b&a)!=0")

  (op 0x11 "IFC" dopfmt
      "performs next instruction only if (b&a)==0")

  (op 0x12 "IFE" dopfmt
      "performs next instruction only if b==a")

  (op 0x13 "IFN" dopfmt
      "performs next instruction only if b!=a")

  (op 0x14 "IFG" dopfmt
      "performs next instruction only if b>a")

  (op 0x15 "IFA" dopfmt
      "performs next instruction only if b>a (signed)")

  (op 0x16 "IFL" dopfmt
      "performs next instruction only if b<a")

  (op 0x17 "IFU" dopfmt
      "performs next instruction only if b<a (signed)")

  (op 0x1a "ADX" dopfmt
      "sets b to b+a+EX, sets EX to 0x0001 if there is an overflow,"
      " 0x0 otherwise.")

  (op 0x1b "SBX" dopfmt
      "sets b to b-a+EX, sets EX to 0xFFFF if there is an under-flow,"
      " 0x0001 if there's an overflow, 0x0 otherwise")

  (op 0x1e "STI" dopfmt
      "sets b to a, then increases I and J by 1")

  (op 0x1f "STD" dopfmt
      "sets b to a, then decreases I and J by 1"))


(defn BRACKET
  ([register const]
     (-> register
         (assoc :deref true)
         (assoc :offset const)))
  ([register]
     (BRACKET register 0)))


(defn CONST [value]
  {:type :constant
   :value value})


;; DCPU specific assembler implementation
;;------------------------------------------------------------------------------
;;------------------------------------------------------------------------------
;; Major components are borrowed from core, but all architecture specific word
;; generation is defined here because in the general case there isn't a common
;; abstraction to work with.

(defn assemble-register [state register]
  ;; Returns a list of one or more elements, the first of which is the inline
  ;; operand, and the possible second of which is a trailing word.
  (let [v (-> (get-in dcpu16 [(:key register)
                              (:value register)
                              :value])
              (bit-or (if (:deref register) 0x08 0x0))
              (bit-or (if (:offset register) 0x10 0x0)))]
    (if (:offset register)
      (list v (:offset register))
      (list v))))


(defn assemble-constant [state const]
  (list 0x1F (:value const)))


(defn assemble-param [state param]
  (case (:type param)
    (:register) (assemble-register state param)
    (:label)    (assemble-constant state
                                   {:type :constant
                                    :value (get-in state
                                                   [:labels (:value param)])})
    (:constant) (assemble-constant state param)))


(defn assemble-opcode [state form]
  (let [args (:params form)
        args (map (partial assemble-param state) args)
        words (mapcat rest args)
        args (map first args)]
    (concat (list (apply bit-fmt
                         (get-in dcpu16 [(:key form)
                                         (:value form)
                                         :fmt])
                         (get-in dcpu16 [(:key form)
                                         (:value form)
                                         :code])
                         args))
            words)))


(defn assemble-label [state label])


(defn assemble-form [state form]
  ((case (:type form)
     :opcode    assemble-opcode
     :label     assemble-label
     :alignment assemble-align
     :pad       assemble-padding)
   state form))


(defn assemble [& forms]
  (first
   (reduce
    (fn [[words state] form]
      (let [[state new-words] (assemble-form state form)]
        (list state
              (concat words new-words))))
    [(list) {}] forms)))
