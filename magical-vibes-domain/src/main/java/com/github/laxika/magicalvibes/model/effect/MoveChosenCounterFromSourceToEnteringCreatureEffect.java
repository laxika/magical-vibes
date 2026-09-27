package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.List;

/**
 * Moves one controller-chosen counter from the source permanent onto the creature that caused an
 * enter-the-battlefield trigger.
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

    @Override
    public boolean usesEnteringPermanentReference() {
        return true;
    }
}
