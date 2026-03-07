package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches cards that have Phyrexian mana symbols ({R/P}, {G/P}, etc.) in their mana cost.
 */
public record PhyrexianManaPredicate() implements CardPredicate {
}
