package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a spell whose mana value, printed power, or printed toughness equals the number chosen
 * on the evaluating source permanent.
 */
public record StackEntryManaValuePowerOrToughnessEqualsSourceChosenNumberPredicate()
        implements StackEntryPredicate {
}
