package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Gives the resolving ability's controller the evaluated number of rad counters. */
public record GiveControllerRadCountersEffect(DynamicAmount amount) implements CardEffect {

    public GiveControllerRadCountersEffect(int amount) {
        this(new Fixed(amount));
    }
}
