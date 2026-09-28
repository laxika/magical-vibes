package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Applies a permanent power/toughness modification to a physical card identity. */
public record PerpetuallyBoostCardEffect(Card card, DynamicAmount powerBoost, DynamicAmount toughnessBoost)
        implements CardEffect {

    public PerpetuallyBoostCardEffect(Card card, int powerBoost, int toughnessBoost) {
        this(card, new Fixed(powerBoost), new Fixed(toughnessBoost));
    }
}
