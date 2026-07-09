package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches players that have been dealt damage this turn (from any source — combat, spells, abilities).
 * Evaluated against {@code GameData.playersDealtDamageThisTurn}.
 */
public record PlayerDealtDamageThisTurnPredicate() implements PlayerPredicate {
}
