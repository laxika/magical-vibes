package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches an opponent whose life total is greater than the evaluating controller's life total.
 * The life comparison is checked when the target is selected; once selected, only the opponent
 * relationship remains relevant for resolution.
 */
public record PlayerHasMoreLifeThanControllerPredicate() implements PlayerPredicate {
}
