package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose mana value is strictly less than the mana value of the permanent that
 * imposed the filter.
 */
public record PermanentManaValueLessThanSourceManaValuePredicate() implements PermanentPredicate {
}
