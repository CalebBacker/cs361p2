package fa.nfa;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fa.State;

/**
 * Models a single state in an NFA.
 * Each state stores its outgoing transitions grouped by symbol.
 *
 * @author trevo
 */
public class NFAState extends State {

	/**
	 * Maps a transition symbol to the set of destination states reachable on that symbol.
	 */
	private final Map<Character, Set<NFAState>> transitions;

	/**
	 * Creates a new NFA state with the given name.
	 *
	 * @param name unique state label
	 */
	public NFAState(String name) {
		super(name);
		this.transitions = new HashMap<Character, Set<NFAState>>();
	}

	/**
	 * Adds a single outgoing transition on the given symbol.
	 *
	 * @param symbol transition label
	 * @param toState destination state
	 */
	public void addTransition(char symbol, NFAState toState) {
		transitions.computeIfAbsent(symbol, key -> new HashSet<NFAState>()).add(toState);
	}

	/**
	 * Adds multiple outgoing transitions on the given symbol.
	 *
	 * @param symbol transition label
	 * @param toStates destination states
	 */
	public void addTransitions(char symbol, Set<NFAState> toStates) {
		transitions.computeIfAbsent(symbol, key -> new HashSet<NFAState>()).addAll(toStates);
	}

	/**
	 * Returns all destination states reachable on the given symbol.
	 * If there are no such transitions, an empty set is returned.
	 *
	 * @param symbol transition label
	 * @return set of reachable states on symbol
	 */
	public Set<NFAState> toStates(char symbol) {
		Set<NFAState> toStates = transitions.get(symbol);
		if (toStates == null) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(toStates);
	}
}
