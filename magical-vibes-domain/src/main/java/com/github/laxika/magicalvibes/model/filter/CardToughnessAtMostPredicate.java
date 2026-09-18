package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card whose printed toughness is less than or equal to {@code maxToughness}. Cards
 * without a toughness never match.
 */
public record CardToughnessAtMostPredicate(int maxToughness) implements CardPredicate {
}
