package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card whose printed toughness is greater than or equal to {@code minToughness}.
 * Cards without a toughness never match.
 */
public record CardToughnessAtLeastPredicate(int minToughness) implements CardPredicate {
}
