package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Gives the player being attacked the evaluated number of rad counters. */
public record GiveDefendingPlayerRadCountersEffect(DynamicAmount amount) implements CardEffect {

    public GiveDefendingPlayerRadCountersEffect(int amount) {
        this(new Fixed(amount));
    }

    @Override
    public boolean referencesEventValue() {
        return amount instanceof EventValue;
    }
}
