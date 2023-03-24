package fi.lipp.regexp.service

import fi.lipp.regexp.service.FiniteAutomaton.Label
import fi.lipp.regexp.service.FiniteAutomaton.Transition
import fi.lipp.regexp.service.Quantifier.*
import java.util.*
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.log2
import kotlin.math.pow

/**
 * The expression in itself can't be used to work out the patterns. We need to create an NFA so that the string is able
 * to traverse and determine if it succeeded the match
 *
 * @see FiniteAutomaton
 */
object RegExInterpreter {
    /**
     * @param expression the parsed expression
     * @return the NFA built from the expression
     */
    fun makeNFA(expression: Expression): FiniteAutomaton {
        val (transitions, _) = make(simplify(expression), 1, 2, 3)
        return FiniteAutomaton(1, setOf(2), transitions)
    }

    /**
     * @param expression the parsed expression
     * @return the DFA built from the expression
     */
    fun makeDFA(expression: Expression): FiniteAutomaton {
        return convertToDFA(makeNFA(expression))
    }

    /**
     * NFA contains many transitions that can be taken despite the input string (Epsilon transitions).
     * This causes a bigger complexity in the RegEx automaton due to heavy backtracking.
     * Having a deterministic automaton will tell the matching algorithm where exactly to go.
     *
     * @param nfa the non-deterministic finite automaton
     * @return the deterministic finite automaton
     */
    fun convertToDFA(nfa: FiniteAutomaton): FiniteAutomaton {
        assert(nfa.ends.size == 1) {
            "NFA can have only 1 end state"
        }

        // Raising all state values to the power of 2
        // Doing this will help in grouping all the nodes of common characteristics
        // without losing any information.
        // In the final automaton, the node number is insignificant
        val binaryNFA = nfa.copy(
            start = 2 raisedTo (nfa.start - 1),
            ends = nfa.ends.map { 2 raisedTo (it - 1) }.toSet(),
            transitions = nfa.transitions.map { t ->
                t.copy(
                    from = 2 raisedTo (t.from - 1),
                    to = 2 raisedTo (t.to - 1)
                )
            }.toSet()
        )
        val labels = binaryNFA.transitions.map(Transition::label).toSet() - Label.Eps

        // Retrieve the epsilon closure of the starting node
        val epsClosureStart = epsilonClosure(binaryNFA.transitions, binaryNFA.start)

        val dfaStartState = collapseToNewState(epsClosureStart)
        val dfaEndStates = mutableSetOf<Int>()
        val dfaTransitions = mutableSetOf<Transition>()

        val unmarkedStates = Stack<Int>().apply { push(dfaStartState) }
        val visited = mutableSetOf<Int>()

        // loop while there are states to explore
        while (unmarkedStates.isNotEmpty()) {
            val fromState = unmarkedStates.pop()

            for (label in labels) {
                // get the epsilon closure of the examining node
                val toStates = epsilonClosure(
                    binaryNFA.transitions,
                    *move(binaryNFA.transitions, expandToShowStates(fromState), label)
                )
                if (toStates.isEmpty())
                    continue
                val toStateCollapsed = collapseToNewState(toStates)
                if (toStateCollapsed !in visited) {
                    unmarkedStates.push(toStateCollapsed)
                    visited.add(toStateCollapsed)
                }

                // Add a transition between the two states
                dfaTransitions.add(
                    Transition(
                        from = fromState,
                        to = toStateCollapsed,
                        label = label
                    )
                )
            }
        }

        // Collect all states and identify the end states
        val dfaStates = mutableSetOf<Int>().apply {
            dfaTransitions.forEach {
                add(it.from)
                add(it.to)
            }
        }
        for (oldEndState in binaryNFA.ends) {
            for (state in dfaStates) {
                // Using the bitwise-AND operation; NO INFORMATION LOST
                if ((state and oldEndState) == oldEndState)
                    dfaEndStates.add(state)
            }
        }

        return FiniteAutomaton(dfaStartState, dfaEndStates, dfaTransitions)
    }

    /**
     * Finding the reachable states from a collection of starting states in the given
     * transition set.
     *
     * @param transitions the set of NFA transitions
     * @param fromStates the set of states in the combined DFA state
     * @param label the label of the specific transition
     * @return an array of next possible nodes
     */
    private fun move(transitions: Set<Transition>, fromStates: Set<Int>, label: Label): IntArray {
        val result = mutableSetOf<Int>()
        for (state in fromStates) {
            transitions.filter { it.label == label && it.from == state }.forEach { result.add(it.to) }
        }
        return result.toIntArray()
    }

    /**
     * Finds the reachable nodes from the given set of states by following the epsilons only.
     *
     * @param transitions the set of NFA transitions
     * @param states the set of starting states
     * @return the epsilon closure
     */
    private fun epsilonClosure(transitions: Set<Transition>, vararg states: Int): Set<Int> {
        val result = mutableSetOf(*(states.toTypedArray()))
        val stack = Stack<Int>()
        states.forEach(stack::push)
        while (stack.isNotEmpty()) {
            val state = stack.pop()
            transitions
                .filter { it.from == state && it.label == Label.Eps }
                .forEach { transition ->
                    result.add(transition.to)
                    stack.push(transition.to)
                }
        }
        return result
    }

    /**
     * @param states the set of numbers to collapse
     * @return a single integer containing the information of all given states
     */
    private fun collapseToNewState(states: Set<Int>) = states.fold(0) { acc, i -> acc or i }

    /**
     * @param state a number to exact information
     * @return a set of numbers that were stored in the given state
     */
    private fun expandToShowStates(state: Int): Set<Int> {
        val states = mutableSetOf<Int>()
        val count = floor(log2(state.toDouble())).toInt()
        for (i in 0..count)
            if (state and (2 raisedTo i) == 2 raisedTo i)
                states.add(2 raisedTo i)
        return states
    }

    /**
     * Finds the power of a number, keeping the data type intact, for code readability.
     */
    private infix fun Int.raisedTo(exp: Int) = toDouble().pow(exp).toInt()

    /**
     * The '+' quantifier can be represented differently.
     * {@code (hello)+} is equivalent to {@code (hello)(hello)*}
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
     * Auxiliary function for building the NFA from the given expression.
     *
     * @param expression the expression
     * @param start the state where the NFA for the expression should start.
     * @param end the state where the NFA for the expression should end.
     * @param next the next available state to build inner NFAs.
     * @return the NFA transitions and the next available state.
     */
    private fun make(
        expression: Expression,
        start: Int,
        end: Int,
        next: Int
    ): Pair<Set<Transition>, Int> {
        return when (expression) {
            is Expression.Exact -> {
                // Build an NFA that transitions from start to end with the label corresponding to the exact value
                setOf(Transition(start, end, Label.Str(expression.value))) to next
            }

            is Expression.Group -> {
                when (expression.quantifier) {
                    NONE -> {
                        // in case of a simple group, make the inner regex with the same parameters
                        make(expression.exp, start, end, next)
                    }

                    else -> {
                        // Build an NFA for the inner regex of the group.
                        // the inner NFA will start from "next" and "next + 1".
                        // If the inner group requires, the next available node is "next + 3"
                        val (nfa, next1) = make(expression.exp, next, next + 1, next + 2)

                        // Define the transitions for the built NFA
                        if (expression.quantifier == OPTIONAL) {
                            setOf(
                                Transition(start, next, Label.Eps),
                                Transition(start, end, Label.Eps),
                                Transition(next + 1, end, Label.Eps)
                            ) + nfa to next1
                        } else {
                            setOf(
                                Transition(start, next, Label.Eps),
                                Transition(start, end, Label.Eps),
                                Transition(next + 1, next, Label.Eps),
                                Transition(next + 1, end, Label.Eps)
                            ) + nfa to next1
                        }
                    }
                }
            }

            is Expression.Sequence -> {
                // Create separate NFAs following the guidelines
                val (nfa1, next1) = make(expression.exp1, start, next, next + 2)
                val (nfa2, next2) = make(expression.exp2, next + 1, end, next1)

                // Join the two branches by connecting "next" and "next + 1"
                setOf(
                    Transition(next, next + 1, Label.Eps),
                ) + nfa1 + nfa2 to next2
            }
        }
    }
}