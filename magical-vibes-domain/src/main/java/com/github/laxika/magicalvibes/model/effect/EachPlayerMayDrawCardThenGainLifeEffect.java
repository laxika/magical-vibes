package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Each player may draw one card; each player who drew this way gains one life. */
public record EachPlayerMayDrawCardThenGainLifeEffect()
        implements CardDrawingEffect, LifeGainEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
    }

    @Override
    public DynamicAmount lifeGainAmount() {
        return new Fixed(1);
    }
}
