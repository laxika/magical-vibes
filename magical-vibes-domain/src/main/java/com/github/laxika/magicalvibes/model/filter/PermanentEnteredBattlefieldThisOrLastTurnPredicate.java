package com.github.laxika.magicalvibes.model.filter;

/**
 * Permanents whose physical card entered the battlefield during the current or immediately
 * preceding turn. A controller must have had a previous turn for the predicate to match.
 */
public record PermanentEnteredBattlefieldThisOrLastTurnPredicate() implements PermanentPredicate {
}
