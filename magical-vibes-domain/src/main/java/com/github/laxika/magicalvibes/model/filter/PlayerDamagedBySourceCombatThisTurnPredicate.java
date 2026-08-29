package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches players dealt combat damage by the ability's own source permanent this turn.
 */
public record PlayerDamagedBySourceCombatThisTurnPredicate() implements PlayerPredicate {
}
