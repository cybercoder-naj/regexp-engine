package fi.lipp.regexp.service

import fi.lipp.regexp.model.Editor
import fi.lipp.regexp.model.InvalidPatternException
import java.lang.StringBuilder
import java.util.*

class RegexpServiceImpl : RegexpService {

    /**
     * Returns indexes of all substrings that match the given pattern in the given editor
     */
    override fun matchAll(editor: Editor, pattern: String): Set<Pair<Int, Int>> {
        assertValidRegEx(pattern)

        val expression = RegExParser(pattern).parse()
        val nda = RegExInterpreter.makeNDA(expression)

        val text = StringBuilder(editor.getText())
        // Used a hack here. Since only ascii characters are allowed,
        // I insert a non-ascii character to denote the position of the carets.
        // Not to be done in production.
        editor.getCarets().forEach { c ->
            text.insert(c.getCaretOffset(), RegexpService.CARET_SYMBOL)
        }

        val indexes = mutableSetOf<Pair<Int, Int>>()
        // Generate all the substrings possible to test them individually.
        // Not the best implementation, but it works.
        for (i in 0..text.length) {
            for (j in i..text.length) {
                if (accept(nda, text.substring(i, j))) // if the individual substring passes, add the indexes
                    indexes.add(i to (j - editor.getCarets().size)) // also adjust for the caret characters
            }
        }

        return indexes
    }

    /**
     * Tests whether there exists a pathway the string traverses the NDA such that
     * it reaches the end state.
     *
     * @param nda NDA of the RegEx
     * @param string input string to test
     * @param state the current state of the NDA to examine
     * @return true is the entire string satisfies the NDA
     */
    private fun accept(nda: NDA, string: String, state: Int = nda.start): Boolean {
        if (state == nda.end && string.isEmpty())
            return true // we've reached the end state while no string is remaining

        // return true if there exists a branch that reaches the final node with no string left to test
        val possibilities = nda.transitions.filter { t -> t.from == state }
        for (possibility in possibilities)
            if (test(nda, string, possibility))
                return true
        return false
    }

    /**
     * Test if the given transition with the string would result in a successful traversal
     * through the NDA
     *
     * @param NDA of the RegEx
     * @param string the testing string
     * @param transition the transition possibility from the NDA
     * @return true if the transition is valid
     */
    private fun test(nda: NDA, string: String, transition: NDA.Transition): Boolean {
        if (transition.label == NDA.Label.Eps) // Eps labels can always be traversed
            return accept(nda, string, transition.to)

        if (transition.label is NDA.Label.Str) {
            val str = transition.label.value
            if (string.startsWith(str)) // traverse on the condition
                return accept(nda, string.substring(str.length), transition.to)
        }

        // no possibility of further traversal from this stage
        return false
    }

    /**
     * @param pattern the regex string initially supplied
     * @throws InvalidPatternException if the regex string is not well-formed
     */
    /*
     * Initially had multiple loops. optimised code to fit in one loop
     */
    private fun assertValidRegEx(pattern: String) {
        val bracketStack = Stack<Int>()
        val allowedEscapes = arrayOf('\\', '?', '*', '+', '(', ')', 'c')
        for (i in pattern.indices) {
            val c = pattern[i]
            if (c.code > 128) // all characters must be ASCII
                throw InvalidPatternException()

            if (c == '\\') {
                if (i + 1 >= pattern.length) // backslash cannot be the last character
                    throw InvalidPatternException()

                if (pattern[i + 1] !in allowedEscapes)
                    throw InvalidPatternException()
            }

            if (c in arrayOf('?', '*', '+')) { // testing quantifiers
                if (i < 1)
                    throw InvalidPatternException()

                if (pattern[i - 1] !in arrayOf('\\', ')')) // quantifiers must be after an R-parenthesis
                    throw InvalidPatternException()
            }

            if (c == '(' && (i == 0 || pattern[i - 1] != '\\')) // unescaped L-parenthesis
                bracketStack.push(i)

            if (c == ')' && (i == 0 || pattern[i - 1] != '\\')) { // unescaped R-parenthesis
                if (bracketStack.isEmpty()) // no matching L-parenthesis
                    throw InvalidPatternException()

                val index = bracketStack.peek()
                if (index == i - 1) // empty group, i.e. "()"
                    throw InvalidPatternException()
                bracketStack.pop()
            }
        }

        if (bracketStack.isNotEmpty()) // no matching R-parenthesis
            throw InvalidPatternException()
    }
}