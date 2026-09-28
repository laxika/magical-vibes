package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

/** Spirit of Resilience's graveyard-departure trigger. */
public record PutCounterThenMayBecomeCopyOfCardsEffect(List<Card> triggeringCards)
        implements CardEffect, TriggeringCardsAwareEffect {

    public PutCounterThenMayBecomeCopyOfCardsEffect() {
        this(List.of());
    }

    public PutCounterThenMayBecomeCopyOfCardsEffect {
        triggeringCards = List.copyOf(triggeringCards);
    }

    @Override
    public CardEffect withTriggeringCards(List<Card> cards) {
        return new PutCounterThenMayBecomeCopyOfCardsEffect(cards);
    }
}
