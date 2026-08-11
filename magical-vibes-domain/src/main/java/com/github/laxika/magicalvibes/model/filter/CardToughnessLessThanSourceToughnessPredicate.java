package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a creature card whose printed toughness is less than the source permanent's effective
 * toughness.
 */
public record CardToughnessLessThanSourceToughnessPredicate() implements CardPredicate {
}
