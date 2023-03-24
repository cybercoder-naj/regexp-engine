package fi.lipp.regexp.service

/**
 * Expression is the program readable version of the regex.
 * The string pattern is parsed into this form.
 */
sealed class Expression {
    /**
     * If the RegEx contains characters, then we need to match those exact characters
     * @property value the exact value
     */
    class Exact(val value: String) : Expression()

    /**
     * This is used when there is a group defined by parenthesis.
     * A group can be followed by a quantifier.
     *
     * @property exp the inner expression
     * @property quantifier the group quantifier
     */
    class Group(val exp: Expression, val quantifier: Quantifier = Quantifier.NONE) : Expression()

    /**
     * This joins two expressions together, in the same level.
     *
     * @property exp1 the first expression.
     * @property exp2 the second expression.
     */
    class Sequence(val exp1: Expression, val exp2: Expression) : Expression()

    override fun toString(): String {
        return when (this) {
            is Exact -> this.value
            is Group -> "($exp)${quantifier.char}"
            is Sequence -> "$exp1$exp2"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true

        if (other == null)
            return false

        if (this is Exact) {
            if (other !is Exact)
                return false
            return this.value == other.value
        }

        if (this is Group) {
            if (other !is Group)
                return false
            return this.exp == other.exp &&
                    this.quantifier == other.quantifier
        }

        if (this is Sequence) {
            if (other !is Sequence)
                return false
            return this.exp1 == other.exp1 &&
                    this.exp2 == other.exp2
        }

        return false
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}
