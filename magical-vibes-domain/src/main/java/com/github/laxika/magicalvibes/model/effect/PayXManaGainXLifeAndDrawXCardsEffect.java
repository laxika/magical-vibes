package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.XValue;

/**
 * On resolution, the controller may pay a cost containing X. If they do, they gain X life and
 * draw X cards.
 */
public record PayXManaGainXLifeAndDrawXCardsEffect(String manaCost)
        implements CardDrawingEffect, LifeGainEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new XValue();
    }

    @Override
    public DynamicAmount lifeGainAmount() {
        return new XValue();
    }
}
