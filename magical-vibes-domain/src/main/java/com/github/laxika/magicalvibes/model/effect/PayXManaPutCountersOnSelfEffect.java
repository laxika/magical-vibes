package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * On resolution, the controller may pay {X}: they choose X up to their available mana, pay it,
 * and put X counters of the specified type on the source permanent. Choosing X=0 declines.
 */
public record PayXManaPutCountersOnSelfEffect(CounterType counterType) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
