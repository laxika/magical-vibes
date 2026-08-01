package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches players that have lost life this turn (from any source — damage included).
 * Evaluated against {@code GameData.lifeLostThisTurn}.
 */
public record PlayerLostLifeThisTurnPredicate() implements PlayerPredicate {
}
