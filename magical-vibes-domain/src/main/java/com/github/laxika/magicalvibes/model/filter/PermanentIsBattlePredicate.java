package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent that is currently a battle (CR 115.4 lists battles among the permanents
 * "any target" admits). Layer-aware when the evaluation carries game data, like
 * {@link PermanentIsPlaneswalkerPredicate}.
 */
public record PermanentIsBattlePredicate() implements PermanentPredicate {
}
