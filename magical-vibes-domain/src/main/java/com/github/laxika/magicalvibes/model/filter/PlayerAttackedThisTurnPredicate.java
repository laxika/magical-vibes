package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches players who declared at least one attacker this turn.
 * Evaluated against {@code GameData.playersDeclaredAttackersThisTurn}.
 */
public record PlayerAttackedThisTurnPredicate() implements PlayerPredicate {
}
