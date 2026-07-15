package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Remove up to {@code amount} counters of the given type from the source permanent, clamped at zero
 * (no-op if it has none). Self-targeting, so trigger collectors carry the source permanent id onto
 * the stack entry. Used by Shrewd Hatchling ("Whenever you cast a blue/red spell, remove a -1/-1
 * counter from this creature").
 */
public record RemoveCounterFromSourceEffect(CounterType counterType, int amount) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(TargetCategory.NONE, false, null, true, 1);
    }
}
