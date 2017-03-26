package haslite

/**
 * Tests that check that the translation works correctly.
 */
class ExecTests extends SemanticTests {

    import org.kiama.attribution.Attribution.initTree
    import org.kiama.util.StringEmitter
    import haslite.SECTree._

    /**
     * Parse some test input, perform semantic analysis checks, expect no
     * semantic errors. Then translate into SEC machine code and run the code.
     * The value `expected` should be the output that we expect from this
     * run.
     */
    def execTest (str : String, expected : String) {
        val tree = parseProgram (str)
        initTree (tree)
        val messages = SemanticAnalysis.errors (tree)
        assert (messages.length === 0, messages.toString)

        val instrs = Translator.translate (tree)
        // println (instrs)

        val emitter = new StringEmitter ()
        val machine = new SECMachine (emitter)

        machine.run (instrs) match {
            case _ : machine.State =>
                // Terminated correctly in some state
                assertResult (expected + "\n", "wrong execution output") (emitter.result ())
            case machine.FatalError (message) =>
                fail (message)
        }
    }


    /**
     * Here be tests!
     */

    test ("an integer literal evaluates to the correct result") {
        execTest ("""
            |1
            """.stripMargin,
            "1")
    }

    test ("an addition evaluates to the correct result") {
        execTest ("""
            |9 + 10
            """.stripMargin,
            "19")
    }

    test ("a subtraction evaluates to the correct result") {
        execTest ("""
            |4 - 3
            """.stripMargin,
            "1")
    }

    test ("a subtraction evaluates to the correct result (negative)") {
        execTest ("""
            |5 - 10
            """.stripMargin,
            "-5")
    }

	  test ("a multiplication evaluates to the correct result") {
        execTest ("""
            |2 * 4
            """.stripMargin,
            "8")
    }

    test ("a division evaluates to the correct result") {
        execTest ("""
            |10 / 5
            """.stripMargin,
            "2")
    }

    test ("integer division evaluates to the correct result") {
        execTest ("""
            |11 / 5
            """.stripMargin,
            "2")
    }

	  test ("a boolean literal evaluates to the correct result") {
        execTest ("""
            |true
            """.stripMargin,
            "true")
    }

    test ("a true less-than conditional expression evaluates to the correct result") {
        execTest ("""
            |if (1 < 2) then 15 else 0
            """.stripMargin,
            "15")
    }

    test ("a false less-than conditional expression evaluates to the correct result") {
        execTest ("""
            |if (4 < 2) then 15 else 0
            """.stripMargin,
            "0")
    }

	  test ("nested conditionals evaluate to the correct result") {
        execTest ("""
            |if (2 < 4) then if(1 < 0) then 1 else 2 else 3
            """.stripMargin,
            "2")
    }

    test ("a single def let evaluates to the correct result") {
        execTest ("""
            |let
            |   x = 1
            |in x
            |""".stripMargin,
            "1")
    }

    test ("a multiple def let evaluates to the correct result (use first def)") {
        execTest ("""
            |let
            |   x = 1;
            |   y = 2
            |in x
            """.stripMargin,
            "1")
    }

    test ("a multiple def let evaluates to the correct result (use second def)") {
        execTest ("""
            |let
            |  x = 1;
            |  y = 2
            |in y
            """.stripMargin,
            "2")
    }

    test ("a multiple def let evaluates to the correct result (use both defs)") {
        execTest ("""
            |let
            |  x = 1;
            |  y = 2
            |in x + y
            """.stripMargin,
            "3")
    }

    test("a let with an if evaluates to the correct result") {
  	     execTest("""
      			|let
      			|	x = 1;
      			|	y = 2
      			|in
      			|	if(x < y) then 1 else 0
      			""".stripMargin, "1")
    }

    test ("a nested let def evaluates to the correct result (use inner let)") {
		    execTest("""
    			|let
    			|	x = 5
    			|in let
    				|	y = 1
    				|in
    				|	y
    		""".stripMargin, "1")
    }

	test ("a nested let def evaluates to the correct result (use outer let)") {
      execTest("""
    			|let
    			|	x = 5
    			|in let
    				| y = 2
    				|in
    				|	x
    		""".stripMargin, "5")
    }

	test ("a nested let def evaluates to the correct result (use both lets)") {
      execTest("""
    			|let
    			|	x = 5
    			|in let
    				| y = x + 1
    				|in
    				|	x + y
    		""".stripMargin, "11")
    }

	test ("a variable can be used again in a nested scope without overriding its outer value") {
      execTest("""
    			|let
    			|	x = 2
    			|in
    			|(let
    				| x = 1
    				|in let
    				|		z = 3
    					|in
    					|	x + z
    			|) * x
    		""".stripMargin, "8")
    }

	test ("a variable used outside of scope throws an error") {
		/*
		execTest("""
			|let
			|	x = 2
			|in
			|	(let
			|		y = 1
			|	in
			|		y
			|	)
			| 	+
			|	(let
			|		z = 2
			|	in
			|		z + y
			|	)
		""".stripMargin, "8")
		*/
		//Should fail. Does fail on latest build.
    //TODO implement expected failure tests
    }


    test("inc example from the spec correctly evaluates to 101") {
        execTest("""
      			|let
      			|	x = 100;
      			|	inc = \ a :: Int -> a + 1
      			|in
      			|	inc x
      			""".stripMargin, "101")
    }

    test("calling a function multiple times evaluates correctly") {
        execTest("""
      			|let
      			|	x = 100;
      			|	inc = \ a :: Int -> a + 1
      			|in
      			|	inc inc inc inc x
      			""".stripMargin, "104")
    }

    test ("a nested let allows a function to call another function") {
        execTest("""
      			|let
      			|	foo = \ a :: Int -> a + 1
      			|in let
      				| bar = \ a :: Int -> foo a + 1
      				|in
      				|	bar 1
      		  """.stripMargin, "3")
    }

    test("a let with two lambdas produces the correct result") {
		    execTest("""
      			|let
      			|	x = 5;
      			|	inc = \ a :: Int -> a + 1;
      			|	deinc = \ a :: Int -> a - 1
      			|in
      			|	inc deinc inc deinc inc inc deinc inc x
      		  """.stripMargin, "7")
    }

  	test("an operation between two lets evaluates to the correct result") {
  	   execTest("""
      			|(let
      			|	a = 3
      			|in
      			|	a + 1
      			|)
      			|*
      			|(let
      			|	b = 10
      			|in
      			|	b - 1
      			|)
      		  """.stripMargin, "36")
  	}

    test("reusing a variable outside its inital scope produces the correct result.") {
		    execTest("""
      			|(let
      			|	a = 3
      			|in
      			|	a + 1
      			|)
      			|*
      			|(let
      			|	b = 12
      			|in
      			|	b * (3 + 4)
      			|)
      			|-
      			|(let
      			|	b = 10
      			|in
      			|	b
      			|)
		        """.stripMargin, "326")
    }

    test("reusing a function name outside its inital scope produces the correct result.") {
        execTest("""
      			|(let
      			|	inc = \ a :: Int -> a + 1
      			|in
      			|	inc 1
      			|)
      			|+
      			|(let
      			|	inc = \ a :: Int -> a + 3
      			|in
      			|	inc 4
      			|)
      		  """.stripMargin, "9")
    }

    test("not using a defined lambda produces the correct result.") {
        execTest("""
      			|(let
      			|	inc = \ a :: Int -> a + 1
      			|in
      			|	5
      			|)
      		  """.stripMargin, "5")
	  }

    test("a variable defined in a let should go out of scope after the let is evaluated") {
		/*
        execTest("""
    				|(let
    				|	x = 100;
    				|	inc = \ a :: Int -> a + 1
    				|in
    				|	inc x) * x
    				""".stripMargin, "")
    */
    //Should fail. Does fail on latest build.
    //TODO implement expected failure tests
	  }

}
