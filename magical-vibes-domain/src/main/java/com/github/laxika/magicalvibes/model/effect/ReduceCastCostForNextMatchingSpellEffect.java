package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Creates a generic cost reduction for the controller's next matching spell this turn. */
public record ReduceCastCostForNextMatchingSpellEffect(CardPredicate predicate, DynamicAmount amount,
                                                       boolean faceDownOnly)
        implements NextMatchingSpellCostEffect {

    public ReduceCastCostForNextMatchingSpellEffect(CardPredicate predicate, DynamicAmount amount) {
        this(predicate, amount, false);
    }

    public ReduceCastCostForNextMatchingSpellEffect(CardPredicate predicate, int amount) {
        this(predicate, new Fixed(amount), false);
    }

    public ReduceCastCostForNextMatchingSpellEffect(CardPredicate predicate, int amount,
                                                    boolean faceDownOnly) {
        this(predicate, new Fixed(amount), faceDownOnly);
    }

    @Override
    public boolean appliesToFaceDownCast(boolean castFaceDown) {
        return !faceDownOnly || castFaceDown;
    }
}
