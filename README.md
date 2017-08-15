# Haslite
A tiny expression based language written in Scala using [Kiama](https://bitbucket.org/inkytonik/kiama) as a part of a learning exercise on programming language design and development.

# Language Features

## Data Types:
Haslite only supports two primitive data types: <b>Integers</b> and <b>Booleans</b>.<br>
Haslite also supports the use of <b>closures/lambda</b> expressions through a Function Type, or <b>FunType</b> for short.

## Operators:
### Arithmetic Operators:
Haslite supports the 4 cornerstones of operators; ``+``, ``-``, ``*``, ``/``</b><br>
Examples: ``2 + 3 * 4`` = ``14``, ``3 * 4 + 2`` = ``14``, ``5 / 4 + 3 * 2 - 5 / 2`` = ``5``.

### Relational Operators:
Haslite only supports two operators for comparison; ``==`` and ``<``<br>
Examples: ``1 < 2`` = ``true``, ``5 < 3`` = ``false``, ``1 == 1`` = ``false``.
<br>Note: !=, >, <=, >= are <b>NOT</b> supported!

## Variable Declaration:
Variable declaration is performed through the use of the ``let`` expression.<br>
A variable name must be of the form ``[a-zA-Z][a-zA-Z0-9_]*`` and not be a reserved keyword. That is to say, it must start with a character and consist only of letters, numbers and/or underscores.<br>
```
let
   a = 5;
   b = a + 1
in
   a * b
```
In the above example two variables are declared, ``a`` and ``b``, for use in the expression ``a * b``. The variables are only in <b>scope</b> inside that expression and once evaluated they are lost forever. This means multiple variables can go by the same name, so be cautious of scoping rules when declaring several variables with the same name.

## Control Flow:
Haslite supports control flows through the use of ``if`` expressions.<br>

Some contrived examples of ways you could use an ``if`` expression:<br>

```if(5 < 3) 15 else 90```
```
let
   a = 5;
   b = 3
in
   if(a < b) a else b
```
```
let
   a = 5;
   b = 3
in
   if(a + b < 0) 0 else a + b
```
```
if(
   (let
      a = 5;
      b = 3;
   in
      a * b)
   <
   (let
      a = 15;
      b = 10;
   in
      a / b)
)
true
else
false
```

## Functions:
Haslite supports function declarations through the following syntax:
``FUNC_NAME = \ ARG_NAME :: ARG_TYPE -> BODY_EXPRESSION``
For example, an increment function:
```
let 
  x   = 100;
  inc = \ a :: Int -> a + 1
in
  inc x
```
```
let
	x = 5;
	inc = \ a :: Int -> a + 1;
	deinc = \ a :: Int -> a - 1
in
	inc deinc inc deinc inc inc deinc inc x
```

## Precedence:
Different programming constructs act at different precident levels (Otherwise nothing would work).<br>
From lowest to highest:
+ ``if``
+ ``<`` and ``==``
+ ``+`` and ``-``
+ ``*`` and ``/``
+ All others (``let``, ``=``, etc.)

## Associativity:

All <b>arithmetic is left associative</b>, meaning when dealing with operators of the same precedence they are grouped left-most first. For example ``1 + 1 + 1`` secretly becomes ``(1 + 1) + 1`` behind the scenes.<br>
All <b>relational operators are non-associative</b>, meaning that you cannot operate on any more than two operands in a single instance. ``a == b == c`` will <b>NOT</b> work, as can be said for ``a < b < c`` or any such amalgam. The only way to perform these operations is two a time, eg. ``a == b``. 
