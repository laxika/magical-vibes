package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a spell whose mana value, including chosen X, is at most the evaluating source
 * permanent's effective power.
 */
public record StackEntryManaValueAtMostSourcePowerPredicate() implements StackEntryPredicate {
}
