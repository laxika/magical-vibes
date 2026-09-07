package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches spells whose total mana spent to cast them is less than their mana value.
 */
public record StackEntryManaSpentLessThanManaValuePredicate() implements StackEntryPredicate {
}
