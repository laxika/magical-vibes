package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** "Remove up to N counters from target permanent," with a resolution-time choice of counters. */
public record RemoveChosenCountersFromTargetPermanentEffect(DynamicAmount amount, boolean exactAmount)
        implements CardEffect {

    public RemoveChosenCountersFromTargetPermanentEffect {
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null");
        }
        if (amount instanceof Fixed fixed && fixed.value() < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
    }

    public RemoveChosenCountersFromTargetPermanentEffect(int amount) {
        this(new Fixed(amount), false);
    }

    public RemoveChosenCountersFromTargetPermanentEffect(DynamicAmount amount) {
        this(amount, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
