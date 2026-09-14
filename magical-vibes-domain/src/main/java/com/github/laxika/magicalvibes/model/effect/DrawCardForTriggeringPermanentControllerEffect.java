package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Draws a card for the controller of the permanent that caused the trigger. */
public record DrawCardForTriggeringPermanentControllerEffect() implements CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
    }
}
