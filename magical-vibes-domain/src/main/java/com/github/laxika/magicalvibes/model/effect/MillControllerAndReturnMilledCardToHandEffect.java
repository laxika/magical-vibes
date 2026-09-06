package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Mills cards from the controller's library, then puts one matching milled card into their hand. */
public record MillControllerAndReturnMilledCardToHandEffect(int count, CardPredicate filter)
        implements CardEffect {
}
