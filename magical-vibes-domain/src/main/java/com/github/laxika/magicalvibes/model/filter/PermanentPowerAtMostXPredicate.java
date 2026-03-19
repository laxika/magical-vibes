package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches creatures whose effective power is at most X, where X comes from the
 * {@link FilterContext#xValue()} at evaluation time. Used for abilities like
 * "Destroy target creature with power X or less" (Aryel, Knight of Windgrace).
 */
public record PermanentPowerAtMostXPredicate() implements PermanentPredicate {
}
