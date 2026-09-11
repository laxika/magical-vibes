package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose card mana value is strictly less than X, where X comes from the
 * {@link FilterContext#xValue()} at evaluation time.
 */
public record PermanentManaValueLessThanXPredicate() implements PermanentPredicate {
}
