package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Creates a generic cost reduction for the controller's next matching spell this turn. */
public record ReduceCastCostForNextMatchingSpellEffect(CardPredicate predicate, DynamicAmount amount)
        implements CardEffect {

    public ReduceCastCostForNextMatchingSpellEffect(CardPredicate predicate, int amount) {
        this(predicate, new Fixed(amount));
    }
}
