package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a spell whose mana value, including chosen X, has the odd/even quality chosen on the
 * evaluating source permanent.
 */
public record StackEntryManaValueParityMatchesSourceChosenParityPredicate() implements StackEntryPredicate {
}
