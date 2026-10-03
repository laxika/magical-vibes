package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/** Exiles a chosen card from hand face down and gives it a foretell cost reduced by {2}. */
public record ExileCardFromHandAndBecomeForetoldEffect(Card chosenCard)
        implements CardEffect, ChosenCardAwareEffect {

    public ExileCardFromHandAndBecomeForetoldEffect() {
        this(null);
    }

    @Override
    public CardEffect withChosenCard(Card card) {
        return new ExileCardFromHandAndBecomeForetoldEffect(card);
    }
}
