package fi.lipp.regexp.service

import fi.lipp.regexp.model.InvalidPatternException

/**
 * Goes through every single character and converts the string into program readable code.
 *
 * @see Expression
 * @see Quantifier
 */
class RegExParser(
    private val text: String
) {
    private var index = -1
    private var currentChar = '\u0000'

    init {
        advance()
    }

    /**
     * Moves to the next character in the text.
     * Sets currentChar to \0 if at the end
     */
    private fun advance() {
        currentChar = if (index < text.length - 1)
            text[++index]
        else
            '\u0000'
    }

    /**
     * @return either an exact matching, or a sequence of two expressions.
     */
    private fun exact(): Expression? {
        val builder = StringBuilder()
        var expression: Expression? = null
        while (currentChar != '\u0000') {
            if (currentChar == '\\') { // identifying escape characters
                advance()
                builder.append(
                    if (currentChar == 'c') // using the non-ascii caret hack
                        RegexpService.CARET_SYMBOL
                    else currentChar
                )
                advance()
            } else if (currentChar == '(') { // start of a group
                if (builder.isNotEmpty()) { // record any previous text
                    expression = if (expression == null)
                        Expression.Exact(builder.toString())
                    else
                        Expression.Sequence(expression, Expression.Exact(builder.toString()))
                    builder.clear()
                }
                val groupExp = group() // grab the group
                expression = if (expression == null) // and add to the expression
                    groupExp
                else
                    Expression.Sequence(expression, groupExp)
            } else if (currentChar == ')') { // close of a group
                if (builder.isNotBlank()) { // record whatever was inside the group
                    expression = if (expression == null)
                        Expression.Exact(builder.toString())
                    else
                        Expression.Sequence(expression, Expression.Exact(builder.toString()))
                    builder.clear()
                }
                break // stop progress since the group is over
            } else {
                builder.append(currentChar)
                advance()
            }
        }
        if (builder.isNotBlank()) { // record any remaining stuff in the builder
            expression = if (expression == null)
                Expression.Exact(builder.toString())
            else
                Expression.Sequence(expression, Expression.Exact(builder.toString()))
        }
        return expression
    }

    /**
     * @return a group expression
     */
    private fun group(): Expression {
        advance()
        val inner = exact() ?: throw InvalidPatternException() // get the inner contents

        advance()
        val groupExp = Expression.Group( // check for quantifiers
            inner, when (currentChar) {
                '*' -> {
                    advance()
                    Quantifier.REPEAT
                }

                '+' -> {
                    advance()
                    Quantifier.ATLEAST
                }

                '?' -> {
                    advance()
                    Quantifier.OPTIONAL
                }

                else -> Quantifier.NONE
            }
        )

        return groupExp
    }

    /**
     * @return the equivalent expression from the regex pattern.
     */
    fun parse(): Expression {
        return exact() ?: throw InvalidPatternException()
    }
}