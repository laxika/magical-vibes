package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Static effect that grants the given miracle cost to matching cards in the controller's hand. */
public record GrantMiracleToCardsInHandEffect(CardPredicate filter, String miracleCost)
        implements MiracleGrantingEffect {

    @Override
    public CardPredicate miracleGrantFilter() {
        return filter;
    }
}
