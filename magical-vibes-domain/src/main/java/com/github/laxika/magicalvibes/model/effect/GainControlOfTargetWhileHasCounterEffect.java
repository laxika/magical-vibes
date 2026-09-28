package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.Objects;

/** Gains control of the target for as long as it has a counter of the given type. */
public record GainControlOfTargetWhileHasCounterEffect(CounterType counterType)
        implements CounterConditionedControlEffect {

    public GainControlOfTargetWhileHasCounterEffect {
        Objects.requireNonNull(counterType, "counterType");
    }

    @Override
    public ControlDuration controlDuration() {
        // The floating effect is stored permanently; the control service removes it when the
        // counter condition stops holding.
        return ControlDuration.PERMANENT;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
