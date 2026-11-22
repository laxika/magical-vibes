package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches players that have lost life this turn (from any source — damage included).
 * Evaluated against {@code GameData.lifeLostThisTurn}.
 */
public record PlayerLostLifeThisTurnPredicate(int minimum) implements PlayerPredicate {

    /** A player who lost any life this turn. */
    public PlayerLostLifeThisTurnPredicate() {
        this(1);
    }
}
