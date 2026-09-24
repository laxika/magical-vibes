package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Replaces damage to this permanent by removing that many counters of the specified type. If the
 * permanent does not have enough counters, it is sacrificed instead.
 */
public record PreventDamageByRemovingCountersOrSacrificeEffect(CounterType counterType)
        implements CardEffect {
}
