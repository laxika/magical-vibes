package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.CardTypesAmongCardsDiscardedThisResolution;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Each player discards one card, then the controller draws one card for each distinct card type
 * among the cards discarded this way.
 */
public record EachPlayerDiscardsOneThenDrawsForEachCardTypeEffect() implements CardDrawingEffect {

    private static final DynamicAmount DRAW_AMOUNT = new CardTypesAmongCardsDiscardedThisResolution();

    @Override
    public DynamicAmount drawnCardAmount() {
        return DRAW_AMOUNT;
    }
}
