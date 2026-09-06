package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card whose power is less than or equal to the source permanent's effective power.
 */
public record CardPowerAtMostSourcePowerPredicate() implements CardPredicate {
}
