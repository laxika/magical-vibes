package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Draws cards for the player whose permanent or spell caused the surrounding trigger. */
public record DrawCardForTriggeringPlayerEffect(DynamicAmount amount) implements CardDrawingEffect {

    public DrawCardForTriggeringPlayerEffect() {
        this(1);
    }

    public DrawCardForTriggeringPlayerEffect(int amount) {
        this(new Fixed(amount));
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return amount;
    }
}
