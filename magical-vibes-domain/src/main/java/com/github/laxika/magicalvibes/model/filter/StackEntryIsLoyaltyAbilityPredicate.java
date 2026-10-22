package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a stack entry that is a loyalty ability (an activated ability with a loyalty cost), as opposed to
 * another ability its planeswalker source has, such as one granted by an Equipment.
 */
public record StackEntryIsLoyaltyAbilityPredicate() implements StackEntryPredicate {
}
