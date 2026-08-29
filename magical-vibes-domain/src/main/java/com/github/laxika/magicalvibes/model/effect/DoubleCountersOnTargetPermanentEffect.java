package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Doubles counters on target permanent (Gilder Bairn and Invigorating Surge).
 * A null counter type doubles every kind; otherwise only the specified kind is doubled.
 */
public record DoubleCountersOnTargetPermanentEffect(CounterType counterType) implements CardEffect {

    public DoubleCountersOnTargetPermanentEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
