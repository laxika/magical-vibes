package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card whose mana value is less than the source planeswalker's loyalty.
 */
public record CardManaValueLessThanSourceLoyaltyPredicate() implements CardPredicate {
}
