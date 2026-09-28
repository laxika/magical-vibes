package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Perpetually gives a random creature card in the controller's hand +X/+X. */
public record PerpetuallyBoostRandomCreatureCardInHandEffect(DynamicAmount powerBoost)
        implements CardEffect {

    public PerpetuallyBoostRandomCreatureCardInHandEffect(int powerBoost) {
        this(new Fixed(powerBoost));
    }
}
