package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/** Exiles a chosen hand card, gives it time counters equal to its mana value, then advances other owned cards in exile. */
public record ExileCardFromHandWithManaValueTimeCountersEffect(Card chosenCard)
        implements CardEffect, ChosenCardAwareEffect {

    public ExileCardFromHandWithManaValueTimeCountersEffect() {
        this(null);
    }

    @Override
    public CardEffect withChosenCard(Card card) {
        return new ExileCardFromHandWithManaValueTimeCountersEffect(card);
    }
}
