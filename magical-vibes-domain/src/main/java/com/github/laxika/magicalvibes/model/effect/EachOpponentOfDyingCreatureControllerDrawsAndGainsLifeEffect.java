package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Each opponent of the controller of the creature that caused the death trigger draws a card and
 * gains the configured amount of life.
 */
public record EachOpponentOfDyingCreatureControllerDrawsAndGainsLifeEffect(int lifeGain)
        implements CardDrawingEffect, LifeGainEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
    }

    @Override
    public DynamicAmount lifeGainAmount() {
        return new Fixed(lifeGain);
    }
}
