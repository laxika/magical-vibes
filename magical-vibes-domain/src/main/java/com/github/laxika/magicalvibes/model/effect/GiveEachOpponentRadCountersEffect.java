package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Gives each opponent of the resolving ability's controller the evaluated number of rad counters. */
public record GiveEachOpponentRadCountersEffect(DynamicAmount amount) implements CardEffect {

    public GiveEachOpponentRadCountersEffect(int amount) {
        this(new Fixed(amount));
    }

    @Override
    public boolean referencesEventValue() {
        return amount instanceof EventValue;
    }
}
