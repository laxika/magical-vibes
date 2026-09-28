package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a spell whose mana value, printed power, or printed toughness equals a fixed number.
 */
public record StackEntryManaValuePowerOrToughnessEqualsPredicate(int number)
        implements StackEntryPredicate {
}
