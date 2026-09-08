package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card whose mana value is at most the source permanent's effective power.
 */
public record CardManaValueAtMostSourcePowerPredicate() implements CardPredicate {
}
