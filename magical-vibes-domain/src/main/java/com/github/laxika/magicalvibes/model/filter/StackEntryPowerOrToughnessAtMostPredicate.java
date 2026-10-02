package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a spell whose printed power or toughness is at most a fixed number.
 */
public record StackEntryPowerOrToughnessAtMostPredicate(int maxValue)
        implements StackEntryPredicate {
}
