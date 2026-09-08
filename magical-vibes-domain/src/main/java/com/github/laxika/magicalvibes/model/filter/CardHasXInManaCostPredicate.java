package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches cards whose mana cost contains an X symbol, regardless of any value chosen while
 * casting the card.
 */
public record CardHasXInManaCostPredicate() implements CardPredicate {
}
