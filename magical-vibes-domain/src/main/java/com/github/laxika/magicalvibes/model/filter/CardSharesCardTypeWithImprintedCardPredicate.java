package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card that shares at least one card type with the card imprinted on the source card.
 * The predicate is resolution-aware: without game state it matches so target enumeration can
 * offer the broad "target card" choice before the activation cost imprints the exiled card.
 * Requiring an imprint compares against all cards still exiled with the source permanent,
 * including cards exiled by repeated resolutions of its linked ability.
 */
public record CardSharesCardTypeWithImprintedCardPredicate(boolean requireImprintedCard) implements CardPredicate {

    public CardSharesCardTypeWithImprintedCardPredicate() {
        this(false);
    }
}
