package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Perpetually gives the topmost creature card in the controller's library +X/+X. */
public record PerpetuallyBoostTopCreatureCardInLibraryEffect(DynamicAmount powerBoost)
        implements CardEffect {

    public PerpetuallyBoostTopCreatureCardInLibraryEffect(int powerBoost) {
        this(new Fixed(powerBoost));
    }
}
