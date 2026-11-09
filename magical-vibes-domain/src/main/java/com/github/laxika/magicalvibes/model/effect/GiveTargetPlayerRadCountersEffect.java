package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/** Gives the targeted player the evaluated number of rad counters. */
public record GiveTargetPlayerRadCountersEffect(DynamicAmount amount)
        implements CardEffect {

    public GiveTargetPlayerRadCountersEffect(int amount) {
        this(new com.github.laxika.magicalvibes.model.amount.Fixed(amount));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
