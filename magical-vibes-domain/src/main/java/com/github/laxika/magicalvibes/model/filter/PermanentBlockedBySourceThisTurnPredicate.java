package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a creature that was blocked by the source permanent during the current turn.
 * Unlike {@link PermanentBlockedBySourcePredicate}, this remains usable after combat state is
 * cleared by reading the turn-scoped attacker-only block set and the source's recorded combat
 * opponent IDs.
 */
public record PermanentBlockedBySourceThisTurnPredicate() implements PermanentPredicate {
}
