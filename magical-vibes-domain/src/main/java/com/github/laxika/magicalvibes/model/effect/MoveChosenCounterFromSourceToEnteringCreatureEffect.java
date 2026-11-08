package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.List;

/**
 * Moves one controller-chosen counter from the source permanent onto "that creature": the stack entry's triggering
 * permanent, read as the ability resolves, never a target (Agent's Toolkit, in a {@link MayEffect}).
 */
public record MoveChosenCounterFromSourceToEnteringCreatureEffect(List<CounterType> counterTypes)
        implements CardEffect {

    public MoveChosenCounterFromSourceToEnteringCreatureEffect {
        counterTypes = List.copyOf(counterTypes);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
