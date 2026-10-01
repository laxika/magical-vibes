package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Records a perpetual power/toughness boost for the card that caused the surrounding trigger. */
public record PerpetuallyBoostTriggeringCardEffect(
        DynamicAmount powerBoost, DynamicAmount toughnessBoost) implements CardEffect {

    public PerpetuallyBoostTriggeringCardEffect(int powerBoost, int toughnessBoost) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost));
    }
}
