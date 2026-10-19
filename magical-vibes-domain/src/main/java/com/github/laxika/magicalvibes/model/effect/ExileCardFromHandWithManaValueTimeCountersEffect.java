package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Exiles a chosen hand card, gives it time counters equal to its mana value, then advances other owned cards in exile.
 * When {@code advancesOtherExiledCards} is false the other exiled cards keep their counters. When {@code gainsSuspend}
 * is true the card gains suspend (CR 702.62) instead of being tracked as an Alaundo-style time-counter card.
 */
public record ExileCardFromHandWithManaValueTimeCountersEffect(Card chosenCard, boolean advancesOtherExiledCards,
                                                               boolean gainsSuspend)
        implements CardEffect, ChosenCardAwareEffect {

    public ExileCardFromHandWithManaValueTimeCountersEffect() {
        this(null, true, false);
    }

    public static ExileCardFromHandWithManaValueTimeCountersEffect gainingSuspend() {
        return new ExileCardFromHandWithManaValueTimeCountersEffect(null, false, true);
    }

    @Override
    public CardEffect withChosenCard(Card card) {
        return new ExileCardFromHandWithManaValueTimeCountersEffect(card, advancesOtherExiledCards, gainsSuspend);
    }
}
