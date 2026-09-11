package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a creature that blocked another creature at some point this turn.
 * The turn-scoped blocker-to-attacker map preserves this status after combat ends.
 */
public record PermanentBlockedThisTurnPredicate() implements PermanentPredicate {
}
