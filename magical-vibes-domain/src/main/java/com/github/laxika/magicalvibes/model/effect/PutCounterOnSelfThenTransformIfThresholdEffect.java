package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.List;

/**
 * Put a counter of the specified type on this permanent, then if the counter count
 * reaches the given threshold, remove all counters of that type and transform.
 * <p>
 * When {@code optional} is true, the player is prompted ("you may") before the
 * counters are removed and the permanent transforms (e.g. Primal Amulet).
 * <p>
 * When {@code onTransformEffects} is non-empty, those effects are appended to the
 * resolving stack entry's effects list after the transform completes (e.g. Treasure Map
 * creates three Treasure tokens on transform).
 * Used by Ludevic's Test Subject, Primal Amulet, Treasure Map, and similar cards.
 */
public record PutCounterOnSelfThenTransformIfThresholdEffect(
        CounterType counterType,
        int threshold,
        boolean optional,
        List<CardEffect> onTransformEffects
) implements CardEffect {

    public PutCounterOnSelfThenTransformIfThresholdEffect(CounterType counterType, int threshold) {
        this(counterType, threshold, false, List.of());
    }

    public PutCounterOnSelfThenTransformIfThresholdEffect(CounterType counterType, int threshold, boolean optional) {
        this(counterType, threshold, optional, List.of());
    }

    @Override
    public boolean isSelfTargeting() { return true; }
}
