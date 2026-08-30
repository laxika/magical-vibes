package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Puts up to the evaluated number of matching cards from hand and/or graveyard onto the battlefield tapped. */
public record PutUpToCardsFromHandOrGraveyardOntoBattlefieldEffect(
        CardPredicate predicate, String label, DynamicAmount maxCount) implements CardEffect {

    public PutUpToCardsFromHandOrGraveyardOntoBattlefieldEffect(CardPredicate predicate, String label,
                                                                  int maxCount) {
        this(predicate, label, fixedPositive(maxCount));
    }

    public PutUpToCardsFromHandOrGraveyardOntoBattlefieldEffect {
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
