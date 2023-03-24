package fi.lipp.regexp.service

/**
 * Quantifiers help determine the number of times a group can occur in the pattern
 *
 * @see Expression.Group
 * @property char representation for displaying the expression
 */
enum class Quantifier(
    val char: Char
) {
    /**
     * No quantifier.
     */
    NONE('\u0000'),

    /**
     * The group can occur either once or not at all
     */
    OPTIONAL('?'),

    /**
     * The group can occur zero or more times.
     */
    REPEAT('*'),

    /**
     * The group can occur one or more times.
     */
    ATLEAST('+');
}
