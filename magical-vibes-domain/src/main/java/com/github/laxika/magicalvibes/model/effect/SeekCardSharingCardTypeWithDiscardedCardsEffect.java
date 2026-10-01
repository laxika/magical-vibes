package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

/** Seeks a card that shares a card type with one of the discarded cards. */
public record SeekCardSharingCardTypeWithDiscardedCardsEffect(List<Card> discardedCards)
        implements CardEffect, TriggeringCardsAwareEffect {

    public SeekCardSharingCardTypeWithDiscardedCardsEffect() {
        this(List.of());
    }

    public SeekCardSharingCardTypeWithDiscardedCardsEffect {
        discardedCards = List.copyOf(discardedCards);
    }

    @Override
    public CardEffect withTriggeringCards(List<Card> cards) {
        return new SeekCardSharingCardTypeWithDiscardedCardsEffect(cards);
    }
}
