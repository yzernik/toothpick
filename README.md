# Toothpick

> I was tought assembler in my second year at school,<br>
> It's kind of like construction work<br>
> With a toothpick, for a tool<br>
> So when I made my senior year I threw my code away<br>
> And learned the way to program, that I still prefer today<br>
> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ~ The Eternal Flame

Toothpick is a toolkit for idiomaticly generating inspectable assembler bytecode
from Clojure. It is intended for use in compilers and translators, but the truly
adventurous could no doubt perform manual code generation and force the JVM to
execute the result.

Need I remind you that no warantee is provided with this software?


## Status

At the moment `experimental` is generous for describing Toothpick's
status. `dangerous` would probably be a more apt description.

**Working Components**

 - `toothpick.architecture` provides a sane and probably stable
   interface for defining the instructions which constitute an
   instruction set.
 - `toothpick.architecture` ISAs provide the information needed to
   build an `(instruction, params)` pair to raw bytes.

**TODO List**

 - `toothpick.assembler` needs to be able to do label & relative jump
   computation. This involves inventing a DSL for writing assembler in
   Clojure, the implementing it. This will probably wind up relying on
   fixed length instructions and will be bounded by code size.
 - `toothpick.assembler` must be suitable for wrapping with macros so
   that snippet definiton and compositon is reasonable.
 - `toothpick.isa.dcpu16` should be finished in terms of
   `toothpick.assembler`. This will probably involve some crazy stuff
   to take care of the fact that the dcpu16 spec allows _every
   instruction_ to span 1, 2 or 3 words thanks to the `next word`
   operand.

## Usage

```Clojure
;; TODO write a badass demo
```

## License

Copyright © 2013 Reid "arrdem" McKenzie

Distributed under the Eclipse Public License, the same as Clojure.
