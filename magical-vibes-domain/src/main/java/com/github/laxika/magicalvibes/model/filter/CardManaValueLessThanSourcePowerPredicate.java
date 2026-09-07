package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card whose mana value is strictly less than the source permanent's effective power.
 */
public record CardManaValueLessThanSourcePowerPredicate() implements CardPredicate {
}
