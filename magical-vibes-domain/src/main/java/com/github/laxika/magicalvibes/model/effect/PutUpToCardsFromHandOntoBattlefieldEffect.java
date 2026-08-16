package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Puts up to {@code maxCount} matching cards from the controller's hand onto the battlefield simultaneously. */
public record PutUpToCardsFromHandOntoBattlefieldEffect(CardPredicate predicate, String label, int maxCount)
        implements CardEffect {

    public PutUpToCardsFromHandOntoBattlefieldEffect {
        if (maxCount < 1) {
            throw new IllegalArgumentException("maxCount must be positive");
        }
    }
}
