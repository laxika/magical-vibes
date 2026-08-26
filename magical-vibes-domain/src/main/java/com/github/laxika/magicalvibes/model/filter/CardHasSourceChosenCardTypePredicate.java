package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches cards with a card type chosen by the source permanent.
 * Requires game state and a source card ID.
 */
public record CardHasSourceChosenCardTypePredicate() implements CardPredicate {
}
