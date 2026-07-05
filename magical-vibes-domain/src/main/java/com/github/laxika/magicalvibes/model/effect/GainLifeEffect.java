package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

public record GainLifeEffect(DynamicAmount amount) implements CardEffect {

    public GainLifeEffect(int amount) {
        this(new Fixed(amount));
    }
}
