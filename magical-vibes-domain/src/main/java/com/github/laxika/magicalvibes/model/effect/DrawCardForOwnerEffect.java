package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Draws cards for the owner of the source card, regardless of who controls it. */
public record DrawCardForOwnerEffect(DynamicAmount amount) implements CardDrawingEffect {

    public DrawCardForOwnerEffect(int amount) {
        this(new Fixed(amount));
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return amount;
    }
}
