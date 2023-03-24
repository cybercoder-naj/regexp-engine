package fi.lipp.regexp.service

/**
 * NDA stands for Non-Deterministic Automaton. It is like a graph structure with labels with a start node, and an end node.
 * It holds a list of transitions which act as edges between nodes.
 *
 * @property start start node
 * @property end end node
 * @property transitions the list of transitions
 *
 * @see Transition
 */
data class NDA(
    val start: Int,
    val end: Int,
    val transitions: Set<Transition>
) {

    /**
     * A model that defines how the graph is connected. Each transition starts and ends at an existing node
     * with a given label.
     *
     * @property from the originating node
     * @property to the destination node
     * @property label the label of the transition
     *
     * @see Label
     */
    data class Transition(
        val from: Int,
        val to: Int,
        val label: Label
    ) {
        override fun toString(): String {
            return "$from --$label-> $to"
        }
    }

    /**
     * Label is the condition of the transition in the group.
     * This determines where the string should traverse to reach the end node
     */
    sealed class Label {
        /**
         * Epsilon is an arbitrary symbol which represents unconditional movement.
         * A transition with the Eps label can be used despite the string.
         */
        object Eps : Label() {
            override fun toString() = "Eps"
        }

        /**
         * Use this when a certain branch of the NDA can be used only if the test string has the label value
         */
        data class Str(val value: String) : Label() {
            override fun toString() = value
        }
    }
}