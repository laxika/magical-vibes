package com.github.laxika.magicalvibes.model.effect;

/** "Remove up to N counters from target permanent," with a resolution-time choice of counters. */
public record RemoveChosenCountersFromTargetPermanentEffect(int amount) implements CardEffect {

    public RemoveChosenCountersFromTargetPermanentEffect {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
