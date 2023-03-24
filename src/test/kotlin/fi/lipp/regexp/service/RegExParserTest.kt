package fi.lipp.regexp.service

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RegExParserTest {

    @Test
    fun `test exact question mark`() {
        val parser = RegExParser("\\?")
        assertEquals(Expression.Exact("?"), parser.parse())
    }

    @Test
    fun `test exact star`() {
        val parser = RegExParser("\\*")
        assertEquals(Expression.Exact("*"), parser.parse())
    }

    @Test
    fun `test exact plus`() {
        val parser = RegExParser("\\+")
        assertEquals(Expression.Exact("+"), parser.parse())
    }

    @Test
    fun `test exact words`() {
        val parser = RegExParser("IdeaVim")
        assertEquals(Expression.Exact("IdeaVim"), parser.parse())
    }

    @Test
    fun `test exact words 2`() {
        val parser = RegExParser("Vim")
        assertEquals(Expression.Exact("Vim"), parser.parse())
    }

    @Test
    fun `test group`() {
        val parser = RegExParser("(Vim)")
        assertEquals(Expression.Group(Expression.Exact("Vim")), parser.parse())
    }

    @Test
    fun `test group with plus`() {
        val parser = RegExParser("(Vim)+")
        assertEquals(Expression.Group(Expression.Exact("Vim"), Quantifier.ATLEAST), parser.parse())
    }

    @Test
    fun `test group with question`() {
        val parser = RegExParser("(Vim)?")
        assertEquals(Expression.Group(Expression.Exact("Vim"), Quantifier.OPTIONAL), parser.parse())
    }

    @Test
    fun `test sequence of texts with group`() {
        val parser = RegExParser("(IdeaVim (is )?(a (Vim)(.)?))")
        assertEquals(
            Expression.Group(
                Expression.Sequence(
                    Expression.Sequence(
                        Expression.Exact("IdeaVim "),
                        Expression.Group(Expression.Exact("is "), Quantifier.OPTIONAL)
                    ),
                    Expression.Group(
                        Expression.Sequence(
                            Expression.Sequence(
                                Expression.Exact("a "),
                                Expression.Group(
                                    Expression.Exact("Vim")
                                )
                            ),
                            Expression.Group(Expression.Exact("."), Quantifier.OPTIONAL)
                        )
                    )
                )
            ), parser.parse()
        )
    }

    @Test
    fun `test sequence of texts with group 2`() {
        val parser = RegExParser("Ho(-ho)*")
        assertEquals(
            Expression.Sequence(
                Expression.Exact("Ho"),
                Expression.Group(Expression.Exact("-ho"), Quantifier.REPEAT)
            ),
            parser.parse()
        )
    }

    @Test
    fun `test nested groups`() {
        val parser = RegExParser("(I(d(e(a(V(i(m)))))))")
        assertEquals(
            Expression.Group(
                Expression.Sequence(
                    Expression.Exact("I"),
                    Expression.Group(
                        Expression.Sequence(
                            Expression.Exact("d"),
                            Expression.Group(
                                Expression.Sequence(
                                    Expression.Exact("e"),
                                    Expression.Group(
                                        Expression.Sequence(
                                            Expression.Exact("a"),
                                            Expression.Group(
                                                Expression.Sequence(
                                                    Expression.Exact("V"),
                                                    Expression.Group(
                                                        Expression.Sequence(
                                                            Expression.Exact("i"),
                                                            Expression.Group(
                                                                Expression.Exact("m"),
                                                            )
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            parser.parse()
        )
    }
}