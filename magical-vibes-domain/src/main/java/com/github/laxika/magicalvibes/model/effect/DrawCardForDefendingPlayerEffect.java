package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Makes the player attacked by the source draw cards. */
public record DrawCardForDefendingPlayerEffect(DynamicAmount amount) implements CardDrawingEffect {

    public DrawCardForDefendingPlayerEffect(int amount) {
        this(new Fixed(amount));
    }

    public DrawCardForDefendingPlayerEffect() {
        this(1);
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return amount;
    }
}
