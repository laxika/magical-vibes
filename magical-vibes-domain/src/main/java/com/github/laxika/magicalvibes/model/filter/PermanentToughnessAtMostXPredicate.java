package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches creatures whose effective toughness is at most X, where X comes from the
 * {@link FilterContext#xValue()} at evaluation time.
 */
public record PermanentToughnessAtMostXPredicate() implements PermanentPredicate {
}
