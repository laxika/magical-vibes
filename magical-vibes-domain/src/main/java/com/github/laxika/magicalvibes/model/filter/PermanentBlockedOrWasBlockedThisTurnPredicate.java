package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a creature that blocked, or was blocked by, another creature at some point this turn —
 * both directions of a block. Reads {@code GameData.combatBlockOpponentIdsThisTurn}, which is
 * recorded at declare-blockers time and is turn-scoped rather than combat-scoped, so the creature
 * still matches in a later combat phase or after combat has ended. Used by Heat Stroke.
 */
public record PermanentBlockedOrWasBlockedThisTurnPredicate() implements PermanentPredicate {
}
