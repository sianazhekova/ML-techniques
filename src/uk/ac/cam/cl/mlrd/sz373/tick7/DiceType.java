package uk.ac.cam.cl.mlrd.sz373.tick7;

//package uk.ac.cam.cl.mlrd.exercises.markov_models;

import java.util.HashMap;
import java.util.Map;

public enum DiceType { //Se  -> set of all 2 emitting hidden states
	START('S'), FAIR('F'), WEIGHTED('W'), END('E');

	private char name;

	private static Map<Character, DiceType> types = new HashMap<>();
	static {
		for (DiceType type : DiceType.values()) {
			types.put(type.name, type);
		}
	}

	private DiceType(char name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return String.valueOf(name);
	}

	/**
	 * Get the die type corresponding to the given character.
	 * 
	 * @param name
	 *            <code>char</code> The char name of the die type
	 * @return {@link Feature} The corresponding die type
	 */
	public static DiceType valueOf(char name) {
		return types.get(name);
	}
}