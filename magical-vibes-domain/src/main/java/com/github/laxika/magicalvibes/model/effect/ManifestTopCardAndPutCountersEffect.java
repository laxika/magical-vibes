package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Manifests the top card of the controller's library, then puts counters on that permanent. */
public record ManifestTopCardAndPutCountersEffect(CounterType counterType, DynamicAmount amount)
        implements CardEffect {

    public ManifestTopCardAndPutCountersEffect(CounterType counterType, int count) {
        this(counterType, new Fixed(count));
    }
}
