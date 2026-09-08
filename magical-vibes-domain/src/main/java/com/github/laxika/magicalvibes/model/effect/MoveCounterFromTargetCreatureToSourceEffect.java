package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Move one counter of the given type from target creature onto the source permanent.
 *
 * <p>Used by upkeep abilities whose destination is the permanent with the ability. Nothing is
 * moved when the source or target is gone, or when the target has no counter of the requested type.</p>
 *
 * @param counterType the kind of counter moved
 */
public record MoveCounterFromTargetCreatureToSourceEffect(CounterType counterType) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
