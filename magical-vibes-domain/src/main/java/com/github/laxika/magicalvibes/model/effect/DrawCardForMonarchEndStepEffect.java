package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Draws one card for the player whose end step is the monarch's end step. */
public record DrawCardForMonarchEndStepEffect()
        implements MonarchEndStepTriggeredEffect, CardDrawingEffect {

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(1);
    }
}
