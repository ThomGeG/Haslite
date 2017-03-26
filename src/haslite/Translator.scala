package haslite

/**
 * Translator from Haslite source programs to SEC target programs.
 */
object Translator {

    import SECTree._
    import HasliteTree._
    import scala.collection.mutable.ListBuffer
    import SemanticAnalysis.tipe

    /**
     * Return a frame that represents the SEC instructions for a Haslite program.
     */
    def translate (program : Program) : Frame = {

        // An instruction buffer for accumulating the program instructions
        val programInstrBuffer = new ListBuffer[Instr] ()

        /**
         * Translate the program by translating its expression.
         */
        val expInstrs = translateExpression (program.exp)
        programInstrBuffer.appendAll (expInstrs)
        programInstrBuffer.append (IPrint ())

        // Gather the program's instructions and return them
        programInstrBuffer.result ()

    }

    /**
     * Translate an expression and return a list of the instructions that
     * form the translation.
     */
    def translateExpression (exp : Exp) : Frame = {
        exp match {

            // Simple expressions. Each of these just corresponds to a single
            // instruction that uses a value from the source tree.

            case BoolExp (value) =>
              List(IBool(value))

            case IdnUse (i) =>
              List(IVar(i))

            case IntExp (value) =>
              List(IInt(value))

            // Application, relational expressions and arithmetic expressions. Generate
            // code to translate the two operand expressions and then generate the
            // appropriate instruction to perform the operation.

            case AppExp (l, r) =>
              translateExpression(l) ++
              translateExpression(r) ++
              List(ICall())

            case EqualExp (l, r) =>
              translateExpression(l) ++
              translateExpression(r) ++
              List(IEqual())

            case LessExp (l, r) =>
              translateExpression(l) ++
              translateExpression(r) ++
              List(ILess())

            case MinusExp (l, r) =>
              translateExpression(l) ++
              translateExpression(r) ++
              List(ISub())

            case PlusExp (l, r) =>
              translateExpression(l) ++
              translateExpression(r) ++
              List(IAdd())

            case SlashExp (l, r) =>
              translateExpression(l) ++
              translateExpression(r) ++
              List(IDiv())

            case StarExp (l, r) =>
              translateExpression(l) ++
              translateExpression(r) ++
              List(IMul())

            // An if expression translate into the code for the condition and a
            // branch instruction containing the code for the left and right
            // sides of the expression.

            case IfExp (c, l, r) =>
              translateExpression(c) ++
              List(IBranch(translateExpression(l), translateExpression(r)))

            case LamExp (IdnDef (name, _), body) =>
              List(IClosure(name, translateExpression(body)))

            case LetExp (Nil, exp) =>
              translateExpression(exp)

            case LetExp ((Defn (n, body))::ds, exp) =>
				       List(IClosure(n, translateExpression(LetExp(ds, exp)))) ++
				       translateExpression(body) ++
				       List(ICall()) ++
				       List(IPopEnv())

        }
    }

}
