package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Puts up to {@code maxCount} matching cards from the controller's hand onto the battlefield simultaneously. */
public record PutUpToCardsFromHandOntoBattlefieldEffect(CardPredicate predicate, String label, DynamicAmount maxCount)
        implements CardEffect {

    public PutUpToCardsFromHandOntoBattlefieldEffect(CardPredicate predicate, String label, int maxCount) {
        this(predicate, label, fixedPositive(maxCount));
    }

    public PutUpToCardsFromHandOntoBattlefieldEffect {
        if (maxCount == null) {
            throw new IllegalArgumentException("maxCount must not be null");
        }
    }

    private static DynamicAmount fixedPositive(int maxCount) {
        if (maxCount < 1) {
            throw new IllegalArgumentException("maxCount must be positive");
        }
        return new Fixed(maxCount);
    }
}
