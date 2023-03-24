package fi.lipp.regexp.service

import fi.lipp.regexp.service.Quantifier.*

/**
 * The expression in itself can't be used to work out the patterns. We need to create an NDA so that the string is able
 * to traverse and determine if it succeeded the match
 *
 * @see NDA
 */
object RegExInterpreter {
    /**
     * @param expression the parsed expression
     * @return the NDA built from the expression
     */
    fun makeNDA(expression: Expression): NDA {
        val (transitions, _) = make(simplify(expression), 1, 2, 3)
        return NDA(1, 2, transitions)
    }

    /**
     * The '+' quantifier can be represented differently.
     * {@code (hello)} is equivalent to {@code (hello)(hello)*}
     *
     * @param expression the expression
     * @return simplified version
     */
    private fun simplify(expression: Expression): Expression {
        return when (expression) {
            is Expression.Group -> {
                when (expression.quantifier) {
                    ATLEAST -> Expression.Sequence(
                        Expression.Group(expression.exp),
                        Expression.Group(expression.exp, REPEAT)
                    )

                    else -> Expression.Group(simplify(expression.exp), expression.quantifier)
                }
            }

            is Expression.Sequence -> Expression.Sequence(simplify(expression.exp1), simplify(expression.exp2))
            else -> expression
        }
    }

    /**
     * Auxiliary function for building the NDA from the given expression.
     *
     * @param expression the expression
     * @param start the node where the NDA for the expression should start.
     * @param end the node where the NDA for the expression should end.
     * @param next the next available node to build inner NDAs.
     * @return the NDA transitions and the next available node.
     */
    private fun make(
        expression: Expression,
        start: Int,
        end: Int,
        next: Int
    ): Pair<Set<NDA.Transition>, Int> {
        return when (expression) {
            is Expression.Exact -> {
                // Build an NDA that transitions from start to end with the label corresponding to the exact value
                setOf(NDA.Transition(start, end, NDA.Label.Str(expression.value))) to next
            }

            is Expression.Group -> {
                when (expression.quantifier) {
                    NONE -> {
                        // in case of a simple group, make the inner regex with the same parameters
                        make(expression.exp, start, end, next)
                    }

                    else -> {
                        // Build an NDA for the inner regex of the group.
                        // the inner NDA will start from "next" and "next + 1".
                        // If the inner group requires, the next available node is "next + 3"
                        val (nda, next1) = make(expression.exp, next, next + 1, next + 2)

                        // Define the transitions for the built NDA
                        if (expression.quantifier == OPTIONAL) {
                            setOf(
                                NDA.Transition(start, next, NDA.Label.Eps),
                                NDA.Transition(start, end, NDA.Label.Eps),
                                NDA.Transition(next + 1, end, NDA.Label.Eps)
                            ) + nda to next1
                        } else {
                            setOf(
                                NDA.Transition(start, next, NDA.Label.Eps),
                                NDA.Transition(start, end, NDA.Label.Eps),
                                NDA.Transition(next + 1, next, NDA.Label.Eps),
                                NDA.Transition(next + 1, end, NDA.Label.Eps)
                            ) + nda to next1
                        }
                    }
                }
            }

            is Expression.Sequence -> {
                // Create separate NDAs following the guidelines
                val (nda1, next1) = make(expression.exp1, start, next, next + 2)
                val (nda2, next2) = make(expression.exp2, next + 1, end, next1)

                // Join the two branches by connecting "next" and "next + 1"
                setOf(
                    NDA.Transition(next, next + 1, NDA.Label.Eps),
                ) + nda1 + nda2 to next2
            }
        }
    }
}