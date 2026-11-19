package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Draws cards for the player the surrounding trigger was about: "its controller" of a triggering permanent, as it is
 * when the ability resolves or as it last existed, or a triggering player such as "that attacking player".
 */
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
