package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a spell whose mana value, including chosen X, equals the number chosen on the
 * evaluating source permanent.
 */
public record StackEntryManaValueEqualsSourceChosenNumberPredicate() implements StackEntryPredicate {
}
