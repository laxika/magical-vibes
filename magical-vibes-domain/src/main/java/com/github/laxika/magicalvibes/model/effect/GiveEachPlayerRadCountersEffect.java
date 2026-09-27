package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Gives every player the evaluated number of rad counters. */
public record GiveEachPlayerRadCountersEffect(DynamicAmount amount) implements CardEffect {

    public GiveEachPlayerRadCountersEffect(int amount) {
        this(new Fixed(amount));
    }
}
