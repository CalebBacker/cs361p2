package fa.nfa;

import fa.State;

import java.util.*;

/**
 * Implementation of a Nondeterministic Finite Automaton (NFA).
 * This class models an NFA with states, transitions, including epsilon transitions.
 * It implements NFAInterface and provides methods for state management, transitions,
 * acceptance simulation, and DFA checking.
 *
 * @author Trevor Fry, Caleb Backer
 */
public class NFA implements NFAInterface {

    private Set<Character> sigma;
    private Map<String, NFAState> states;
    private NFAState startState;
    private Set<NFAState> finalStates;

    /**
     * Creates a new NFA instance with empty states, alphabet, and no start or final states.
     */
    public NFA() {
        sigma = new HashSet<>();
        states = new HashMap<>();
        finalStates = new HashSet<>();
    }

    @Override
    /**
     * Adds a state to the NFA.
     * @param name the label of the state
     * @return true if a new state was created successfully, false if a state with that name already exists
     */
    public boolean addState(String name) {
        if (states.containsKey(name)) {
            return false;
        }
        states.put(name, new NFAState(name));
        return true;
    }

    @Override
    /**
     * Marks an existing state as a final (accepting) state.
     * @param name the label of the state
     * @return true if successful, false if no state with that name exists
     */
    public boolean setFinal(String name) {
        NFAState state = states.get(name);
        if (state == null) {
            return false;
        }
        finalStates.add(state);
        return true;
    }

    @Override
    /**
     * Sets the start state of the NFA.
     * @param name the label of the start state
     * @return true if successful, false if no state with that name exists
     */
    public boolean setStart(String name) {
        NFAState state = states.get(name);
        if (state == null) {
            return false;
        }
        startState = state;
        return true;
    }

    @Override
    /**
     * Adds a symbol to the alphabet (Sigma).
     * @param symbol the symbol to add
     */
    public void addSigma(char symbol) {
        sigma.add(symbol);
    }

    @Override
    /**
     * Simulates the NFA on the input string to determine if it is accepted.
     * @param s the input string
     * @return true if the string is in the language of the NFA, false otherwise
     */
    public boolean accepts(String s) {
        if (startState == null) {
            return false;
        }
        Set<NFAState> current = eClosure(startState);
        for (char c : s.toCharArray()) {
            Set<NFAState> next = new HashSet<>();
            for (NFAState state : current) {
                next.addAll(getToState(state, c));
            }
            current = new HashSet<>();
            for (NFAState state : next) {
                current.addAll(eClosure(state));
            }
        }
        for (NFAState state : current) {
            if (finalStates.contains(state)) {
                return true;
            }
        }
        return false;
    }

    @Override
    /**
     * Returns the alphabet (Sigma) of the NFA.
     * @return a set of characters in the alphabet
     */
    public Set<Character> getSigma() {
        return new HashSet<>(sigma);
    }

    @Override
    /**
     * Returns the state with the given name.
     * @param name the name of the state
     * @return the NFAState object, or null if no such state exists
     */
    public NFAState getState(String name) {
        return states.get(name);
    }

    @Override
    /**
     * Checks if the state with the given name is a final state.
     * @param name the name of the state
     * @return true if the state exists and is final, false otherwise
     */
    public boolean isFinal(String name) {
        NFAState state = states.get(name);
        return state != null && finalStates.contains(state);
    }

    @Override
    /**
     * Checks if the state with the given name is the start state.
     * @param name the name of the state
     * @return true if the state exists and is the start state, false otherwise
     */
    public boolean isStart(String name) {
        NFAState state = states.get(name);
        return state != null && state == startState;
    }

    @Override
    /**
     * Returns the set of states reachable from the given state on the given symbol.
     * @param from the source state
     * @param onSymb the transition symbol
     * @return a set of destination states
     */
    public Set<NFAState> getToState(NFAState from, char onSymb) {
        return from.toStates(onSymb);
    }

    @Override
    /**
     * Computes the epsilon closure of the given state.
     * The epsilon closure is the set of states reachable from the state via epsilon transitions.
     * @param s the starting state
     * @return the set of states in the epsilon closure
     */
    public Set<NFAState> eClosure(NFAState s) {
        Set<NFAState> closure = new HashSet<>();
        Stack<NFAState> stack = new Stack<>();
        stack.push(s);
        closure.add(s);
        while (!stack.isEmpty()) {
            NFAState current = stack.pop();
            for (NFAState next : current.toStates('e')) {
                if (!closure.contains(next)) {
                    closure.add(next);
                    stack.push(next);
                }
            }
        }
        return closure;
    }

    @Override
    /**
     * Determines the maximum number of NFA copies (active states) created during processing of the string.
     * @param s the input string
     * @return the maximum number of active states
     */
    public int maxCopies(String s) {
        if (startState == null) {
            return 1;
        }
        Set<NFAState> current = eClosure(startState);
        int max = current.size();
        for (char c : s.toCharArray()) {
            Set<NFAState> next = new HashSet<>();
            for (NFAState state : current) {
                next.addAll(getToState(state, c));
            }
            current = new HashSet<>();
            for (NFAState state : next) {
                current.addAll(eClosure(state));
            }
            max = Math.max(max, current.size());
        }
        return max;
    }

    @Override
    /**
     * Adds a transition to the NFA.
     * @param fromState the name of the source state
     * @param toStates the set of names of the destination states
     * @param onSymb the transition symbol
     * @return true if successful, false if states don't exist or symbol is not in alphabet (except 'e')
     */
    public boolean addTransition(String fromState, Set<String> toStates, char onSymb) {
        NFAState from = states.get(fromState);
        if (from == null || !sigma.contains(onSymb) && onSymb != 'e') {
            return false;
        }
        Set<NFAState> toNFAStates = new HashSet<>();
        for (String to : toStates) {
            NFAState toState = states.get(to);
            if (toState == null) {
                return false;
            }
            toNFAStates.add(toState);
        }
        from.addTransitions(onSymb, toNFAStates);
        return true;
    }

    @Override
    /**
     * Checks if the NFA has DFA properties (no epsilon transitions, deterministic transitions).
     * @return true if the NFA is a DFA, false otherwise
     */
    public boolean isDFA() {
        for (NFAState state : states.values()) {
            // Check for epsilon transitions
            if (!state.toStates('e').isEmpty()) {
                return false;
            }
            // Check for multiple transitions per symbol
            for (char sym : sigma) {
                if (state.toStates(sym).size() > 1) {
                    return false;
                }
            }
        }
        return true;
    }
}