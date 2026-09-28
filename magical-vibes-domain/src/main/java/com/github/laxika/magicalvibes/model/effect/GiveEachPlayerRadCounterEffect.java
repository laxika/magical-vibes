package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Gives the evaluated number of rad counters to each player. */
public record GiveEachPlayerRadCounterEffect(DynamicAmount amount) implements CardEffect {

    public GiveEachPlayerRadCounterEffect() {
        this(new Fixed(1));
    }

    public GiveEachPlayerRadCounterEffect(int amount) {
        this(new Fixed(amount));
    }

    @Override
    public boolean referencesEventValue() {
        return amount instanceof EventValue;
    }
}
