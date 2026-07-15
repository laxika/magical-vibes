package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Removes all counters of the specified type from this permanent and then transforms it.
 * Used as the "accepted" half of a "you may remove counters and transform" ability
 * (e.g. Primal Amulet).
 */
public record RemoveCountersAndTransformSelfEffect(CounterType counterType) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(TargetCategory.NONE, false, null, true, 1);
    }
}
