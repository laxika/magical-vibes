package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card that shares at least one card type with the card imprinted on the source card.
 * The predicate is resolution-aware: without game state it matches so target enumeration can
 * offer the broad "target card" choice before the activation cost imprints the exiled card.
 */
public record CardSharesCardTypeWithImprintedCardPredicate() implements CardPredicate {
}
