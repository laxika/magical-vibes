package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Gives the targeted player the evaluated number of rad counters. */
public record GiveTargetPlayerRadCountersEffect(DynamicAmount amount) implements CardEffect {

    public GiveTargetPlayerRadCountersEffect(int amount) {
        this(new Fixed(amount));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
