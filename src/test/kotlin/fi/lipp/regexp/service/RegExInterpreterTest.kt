package fi.lipp.regexp.service

import org.junit.jupiter.api.Test
import kotlin.math.exp
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

class RegExInterpreterTest {

    @Test
    fun `test simple nda`() {
        val parser = RegExParser("Vim")
        val expression = parser.parse()

        val nda = RegExInterpreter.makeNDA(expression)
        assertEquals(NDA(1, 2, setOf(NDA.Transition(1, 2, NDA.Label.Str("Vim")))), nda)
    }

    @Test
    fun `test simple optional nda`() {
        val parser = RegExParser("a(bc)?")
        val expression = parser.parse()

        val nda = RegExInterpreter.makeNDA(expression)
        assertSetEquals(
            setOf(
                NDA.Transition(3, 4, NDA.Label.Eps),
                NDA.Transition(1, 3, NDA.Label.Str("a")),
                NDA.Transition(4, 5, NDA.Label.Eps),
                NDA.Transition(4, 2, NDA.Label.Eps),
                NDA.Transition(6, 2, NDA.Label.Eps),
                NDA.Transition(5, 6, NDA.Label.Str("bc")),
            ),
            nda.transitions
        )
    }

    @Test
    fun `test simple repeat nda`() {
        val parser = RegExParser("a(bc)*")
        val expression = parser.parse()

        val nda = RegExInterpreter.makeNDA(expression)
        assertSetEquals(
            setOf(
                NDA.Transition(3, 4, NDA.Label.Eps),
                NDA.Transition(1, 3, NDA.Label.Str("a")),
                NDA.Transition(4, 5, NDA.Label.Eps),
                NDA.Transition(4, 2, NDA.Label.Eps),
                NDA.Transition(6, 2, NDA.Label.Eps),
                NDA.Transition(6, 5, NDA.Label.Eps),
                NDA.Transition(5, 6, NDA.Label.Str("bc")),
            ),
            nda.transitions
        )
    }

    @Test
    fun `test simple atleast nda`() {
        val parser = RegExParser("a(bc)+")
        val expression = parser.parse()

        val nda = RegExInterpreter.makeNDA(expression)
        assertSetEquals(
            setOf(
                NDA.Transition(3, 4, NDA.Label.Eps),
                NDA.Transition(1, 3, NDA.Label.Str("a")),
                NDA.Transition(5, 6, NDA.Label.Eps),
                NDA.Transition(6, 7, NDA.Label.Eps),
                NDA.Transition(6, 2, NDA.Label.Eps),
                NDA.Transition(8, 7, NDA.Label.Eps),
                NDA.Transition(8, 2, NDA.Label.Eps),
                NDA.Transition(4, 5, NDA.Label.Str("bc")),
                NDA.Transition(7, 8, NDA.Label.Str("bc")),
            ),
            nda.transitions
        )
    }

    @Test
    fun `test repeat loop nda`() {
        val parser = RegExParser("(ab)?(d)+")
        val expression = parser.parse()

        val nda = RegExInterpreter.makeNDA(expression)
        assertSetEquals(
            setOf(
                NDA.Transition(5, 6, NDA.Label.Str("ab")),
                NDA.Transition(4, 7, NDA.Label.Str("d")),
                NDA.Transition(9, 10, NDA.Label.Str("d")),
                NDA.Transition(1, 5, NDA.Label.Eps),
                NDA.Transition(1, 3, NDA.Label.Eps),
                NDA.Transition(3, 4, NDA.Label.Eps),
                NDA.Transition(6, 3, NDA.Label.Eps),
                NDA.Transition(7, 8, NDA.Label.Eps),
                NDA.Transition(8, 9, NDA.Label.Eps),
                NDA.Transition(8, 2, NDA.Label.Eps),
                NDA.Transition(10, 9, NDA.Label.Eps),
                NDA.Transition(10, 2, NDA.Label.Eps),
            ),
            nda.transitions
        )
    }

    @Test
    fun `test complex nda`() {
        val parser = RegExParser("(IdeaVim (is )?(a (Vim)(.)?))")
        val expression = parser.parse()

        val nda = RegExInterpreter.makeNDA(expression)

        assertSetEquals(
            setOf(
                NDA.Transition(1, 5, NDA.Label.Str("IdeaVim ")),
                NDA.Transition(7, 8, NDA.Label.Str("is ")),
                NDA.Transition(4, 11, NDA.Label.Str("a ")),
                NDA.Transition(12, 9, NDA.Label.Str("Vim")),
                NDA.Transition(13, 14, NDA.Label.Str(".")),
                NDA.Transition(5, 6, NDA.Label.Eps),
                NDA.Transition(6, 7, NDA.Label.Eps),
                NDA.Transition(6, 3, NDA.Label.Eps),
                NDA.Transition(8, 3, NDA.Label.Eps),
                NDA.Transition(3, 4, NDA.Label.Eps),
                NDA.Transition(11, 12, NDA.Label.Eps),
                NDA.Transition(9, 10, NDA.Label.Eps),
                NDA.Transition(14, 2, NDA.Label.Eps),
                NDA.Transition(10, 2, NDA.Label.Eps),
                NDA.Transition(10, 13, NDA.Label.Eps),
            ), nda.transitions
        )
    }

    private fun assertSetEquals(expected: Set<NDA.Transition>, actual: Set<NDA.Transition>) {
        val first = mutableSetOf(*(expected.toTypedArray()))
        val second = mutableSetOf(*(actual.toTypedArray()))
        first.removeAll(actual)
        second.removeAll(expected)

        assertEquals(first.size, 0)
        assertEquals(second.size, 0)
    }
}