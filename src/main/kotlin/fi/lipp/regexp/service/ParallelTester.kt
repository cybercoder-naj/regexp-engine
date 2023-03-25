package fi.lipp.regexp.service

import java.util.concurrent.RecursiveTask

class ParallelTester(
    private val string: String,
    private val automaton: FiniteAutomaton,
    private val start: Int,
    private val end: Int,
    private val result: MutableSet<Pair<Int, Int>>,
    private val caretCount: Int
) : RecursiveTask<Unit>() {

    companion object {
        const val NUM_THREADS = 10
    }

    private val threshold: Int
        get() = if (string.length / NUM_THREADS < 1)
            string.length
        else string.length / NUM_THREADS

    override fun compute() {
        if (end - start <= 10) { // Some threshold
            for (i in start until end) {
                val indexesFromStart = mutableSetOf<Int>()
                accept(automaton, string.substring(i).take(end - start), i, indexesFromStart)
                indexesFromStart.forEach { result.add(i to (it - caretCount)) }
            }
        }

        val mid = (end + start) / 2
        val first = ParallelTester(string, automaton, start, mid, result, caretCount)
        first.fork()

        val second = ParallelTester(string, automaton, mid, end, result, caretCount)
        second.compute()

        first.join()
    }

    /**
     * Tests whether there exists a pathway the string traverses the NFA such that
     * it reaches the end state.
     *
     * @param finiteAutomaton NFA of the RegEx
     * @param string input string to test
     * @param index the current index of the test string
     * @param indexes the list of accepted end indices
     * @param state the current state of the NFA to examine
     * @return true is the entire string satisfies the NFA
     */
    private fun accept(
        finiteAutomaton: FiniteAutomaton,
        string: String,
        index: Int,
        indexes: MutableSet<Int>,
        state: Int = finiteAutomaton.start
    ) {
        if (state in finiteAutomaton.ends) {
            indexes.add(index)
        }

        // test all the possible paths from the Automaton
        val possibilities = finiteAutomaton.transitions.filter { t -> t.from == state }
        for (possibility in possibilities)
            test(finiteAutomaton, string, possibility, index, indexes)
    }

    /**
     * Test if the given transition with the string would result in a successful traversal
     * through the NFA
     *
     * @param finiteAutomaton of the RegEx
     * @param string the testing string
     * @param transition the transition possibility from the NFA
     * @param index the current index of the test string
     * @param indexes the list of accepted end indices
     * @return true if the transition is valid
     */
    private fun test(
        finiteAutomaton: FiniteAutomaton,
        string: String,
        transition: FiniteAutomaton.Transition,
        index: Int,
        indexes: MutableSet<Int>
    ) {
        // Improved from previous implementation (Commit 3228ed15d29b6631a0233ff4c186328d1700a86f)
        // DFA does not involve Eps, can be ignored
        /* if (transition.label == FiniteAutomaton.Label.Eps) // Eps labels can always be traversed
            return accept(finiteAutomaton, string, index, indexes, transition.to) */

        if (transition.label is FiniteAutomaton.Label.Str) {
            val str = transition.label.value
            if (string.startsWith(str)) // traverse on the condition
                return accept(finiteAutomaton, string.substring(str.length), index + str.length, indexes, transition.to)
        }

        // no possibility of further traversal from this stage
    }
}