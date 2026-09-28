package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a creature card whose printed power is strictly less than the source permanent's
 * effective power.
 */
public record CardPowerLessThanSourcePowerPredicate() implements CardPredicate {
}
