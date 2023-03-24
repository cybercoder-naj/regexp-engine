package fi.lipp.regexp.service

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RegExInterpreterTest {

    @Test
    fun `test simple nfa`() {
        val parser = RegExParser("Vim")
        val expression = parser.parse()

        val nfa = RegExInterpreter.makeNFA(expression)
        assertEquals(
            FiniteAutomaton(
                1,
                setOf(2),
                setOf(FiniteAutomaton.Transition(1, 2, FiniteAutomaton.Label.Str("Vim")))
            ), nfa
        )
    }

    @Test
    fun `test simple optional nfa`() {
        val parser = RegExParser("a(bc)?")
        val expression = parser.parse()

        val nfa = RegExInterpreter.makeNFA(expression)
        assertSetEquals(
            setOf(
                FiniteAutomaton.Transition(3, 4, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(1, 3, FiniteAutomaton.Label.Str("a")),
                FiniteAutomaton.Transition(4, 5, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(4, 2, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(6, 2, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(5, 6, FiniteAutomaton.Label.Str("bc")),
            ),
            nfa.transitions
        )
    }

    @Test
    fun `test simple repeat nfa`() {
        val parser = RegExParser("a(bc)*")
        val expression = parser.parse()

        val nfa = RegExInterpreter.makeNFA(expression)
        assertSetEquals(
            setOf(
                FiniteAutomaton.Transition(3, 4, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(1, 3, FiniteAutomaton.Label.Str("a")),
                FiniteAutomaton.Transition(4, 5, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(4, 2, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(6, 2, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(6, 5, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(5, 6, FiniteAutomaton.Label.Str("bc")),
            ),
            nfa.transitions
        )
    }

    @Test
    fun `test simple atleast nfa`() {
        val parser = RegExParser("a(bc)+")
        val expression = parser.parse()

        val nfa = RegExInterpreter.makeNFA(expression)
        assertSetEquals(
            setOf(
                FiniteAutomaton.Transition(3, 4, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(1, 3, FiniteAutomaton.Label.Str("a")),
                FiniteAutomaton.Transition(5, 6, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(6, 7, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(6, 2, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(8, 7, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(8, 2, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(4, 5, FiniteAutomaton.Label.Str("bc")),
                FiniteAutomaton.Transition(7, 8, FiniteAutomaton.Label.Str("bc")),
            ),
            nfa.transitions
        )
    }

    @Test
    fun `test repeat loop nfa`() {
        val parser = RegExParser("(ab)?(d)+")
        val expression = parser.parse()

        val nfa = RegExInterpreter.makeNFA(expression)
        assertSetEquals(
            setOf(
                FiniteAutomaton.Transition(5, 6, FiniteAutomaton.Label.Str("ab")),
                FiniteAutomaton.Transition(4, 7, FiniteAutomaton.Label.Str("d")),
                FiniteAutomaton.Transition(9, 10, FiniteAutomaton.Label.Str("d")),
                FiniteAutomaton.Transition(1, 5, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(1, 3, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(3, 4, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(6, 3, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(7, 8, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(8, 9, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(8, 2, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(10, 9, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(10, 2, FiniteAutomaton.Label.Eps),
            ),
            nfa.transitions
        )
    }

    @Test
    fun `test complex nfa`() {
        val parser = RegExParser("(IdeaVim (is )?(a (Vim)(.)?))")
        val expression = parser.parse()

        val nfa = RegExInterpreter.makeNFA(expression)

        assertSetEquals(
            setOf(
                FiniteAutomaton.Transition(1, 5, FiniteAutomaton.Label.Str("IdeaVim ")),
                FiniteAutomaton.Transition(7, 8, FiniteAutomaton.Label.Str("is ")),
                FiniteAutomaton.Transition(4, 11, FiniteAutomaton.Label.Str("a ")),
                FiniteAutomaton.Transition(12, 9, FiniteAutomaton.Label.Str("Vim")),
                FiniteAutomaton.Transition(13, 14, FiniteAutomaton.Label.Str(".")),
                FiniteAutomaton.Transition(5, 6, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(6, 7, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(6, 3, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(8, 3, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(3, 4, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(11, 12, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(9, 10, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(14, 2, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(10, 2, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(10, 13, FiniteAutomaton.Label.Eps),
            ), nfa.transitions
        )
    }

    @Test
    fun `simple convert nfa to dfa`() {
        val nfa = FiniteAutomaton(
            1, setOf(2), setOf(
                FiniteAutomaton.Transition(1, 2, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(1, 3, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(3, 4, FiniteAutomaton.Label.Str("x")),
                FiniteAutomaton.Transition(4, 3, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(4, 2, FiniteAutomaton.Label.Eps),
            )
        )

        val dfa = RegExInterpreter.convertToDFA(nfa)
        assertEquals(7, dfa.start)
        assertSetEquals(setOf(7, 14), dfa.ends)

        assertSetEquals(
            setOf(
                FiniteAutomaton.Transition(7, 14, FiniteAutomaton.Label.Str("x")),
                FiniteAutomaton.Transition(14, 14, FiniteAutomaton.Label.Str("x")),
            ), dfa.transitions
        )
    }

    @Test
    fun `complex convert nfa to dfa`() {
        val nfa = FiniteAutomaton(1, setOf(2),
            setOf(
                FiniteAutomaton.Transition(5, 6, FiniteAutomaton.Label.Str("ab")),
                FiniteAutomaton.Transition(4, 7, FiniteAutomaton.Label.Str("d")),
                FiniteAutomaton.Transition(9, 10, FiniteAutomaton.Label.Str("d")),
                FiniteAutomaton.Transition(1, 5, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(1, 3, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(3, 4, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(6, 3, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(7, 8, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(8, 9, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(8, 2, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(10, 9, FiniteAutomaton.Label.Eps),
                FiniteAutomaton.Transition(10, 2, FiniteAutomaton.Label.Eps),
            )
        )

        val dfa = RegExInterpreter.convertToDFA(nfa)
        assertEquals(29, dfa.start)
        assertSetEquals(setOf(450, 770), dfa.ends)


        assertSetEquals(
            setOf(
                FiniteAutomaton.Transition(29, 44, FiniteAutomaton.Label.Str("ab")),
                FiniteAutomaton.Transition(29, 450, FiniteAutomaton.Label.Str("d")),
                FiniteAutomaton.Transition(44, 450, FiniteAutomaton.Label.Str("d")),
                FiniteAutomaton.Transition(450, 770, FiniteAutomaton.Label.Str("d")),
                FiniteAutomaton.Transition(770, 770, FiniteAutomaton.Label.Str("d")),
            ), dfa.transitions
        )
    }

    private fun <T> assertSetEquals(expected: Set<T>, actual: Set<T>) {
        val first = mutableSetOf<T>().apply { addAll(expected) }
        val second = mutableSetOf<T>().apply { addAll(actual) }
        first.removeAll(actual)
        second.removeAll(expected)

        assertEquals(first.size, 0)
        assertEquals(second.size, 0)
    }
}