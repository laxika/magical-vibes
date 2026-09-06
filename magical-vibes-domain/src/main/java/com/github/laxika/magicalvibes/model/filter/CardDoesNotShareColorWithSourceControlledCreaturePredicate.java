package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card whose effective colors do not overlap those of any creature controlled by the
 * source permanent.
 */
public record CardDoesNotShareColorWithSourceControlledCreaturePredicate() implements CardPredicate {
}
