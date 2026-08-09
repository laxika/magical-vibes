package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Removes one counter of the given type from the source permanent, or sacrifices it when no such
 * counter remains. Used for upkeep abilities such as fading.
 */
public record RemoveCounterOrSacrificeSelfEffect(CounterType counterType) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
