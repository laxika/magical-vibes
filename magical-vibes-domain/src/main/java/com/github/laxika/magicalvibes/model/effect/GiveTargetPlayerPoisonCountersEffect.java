package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/** Gives the targeted player the evaluated number of poison counters. */
public record GiveTargetPlayerPoisonCountersEffect(DynamicAmount amount) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
