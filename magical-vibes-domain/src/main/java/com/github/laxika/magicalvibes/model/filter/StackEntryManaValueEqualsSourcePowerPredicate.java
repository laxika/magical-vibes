package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a spell whose mana value, including chosen X, equals the evaluating source permanent's
 * effective power.
 */
public record StackEntryManaValueEqualsSourcePowerPredicate() implements StackEntryPredicate {
}
