package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Mills cards from the controller's library, then returns every matching milled card to hand. */
public record MillControllerAndReturnMilledCardsToHandEffect(int count, CardPredicate filter)
        implements CardEffect {
}
