package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Remove up to {@code amount} counters of the given type from the source permanent, clamped at zero
 * (no-op if it has none). A dynamic amount is evaluated on resolution. The player-targeting form
 * uses the stack entry's target player for that amount while still removing counters from the source
 * permanent. Used by Shrewd Hatchling and Descendant of Masumaro.
 */
public record RemoveCounterFromSourceEffect(CounterType counterType, int amount, DynamicAmount dynamicAmount,
                                            boolean targetsPlayer) implements CardEffect {

    public RemoveCounterFromSourceEffect(CounterType counterType, int amount) {
        this(counterType, amount, null, false);
    }

    public RemoveCounterFromSourceEffect(CounterType counterType, DynamicAmount dynamicAmount) {
        this(counterType, 0, dynamicAmount, false);
    }

    public RemoveCounterFromSourceEffect(CounterType counterType, DynamicAmount dynamicAmount,
                                         boolean targetsPlayer) {
        this(counterType, 0, dynamicAmount, targetsPlayer);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetsPlayer
                ? TargetSpec.benign(TargetPredicates.player())
                : new TargetSpec(null, false, null, true, 1);
    }
}
