package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card whose printed power plus toughness is less than or equal to {@code maxTotal}.
 * Cards without either characteristic never match.
 */
public record CardPowerToughnessTotalAtMostPredicate(int maxTotal) implements CardPredicate {
}
