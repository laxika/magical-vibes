package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Moves one counter of the given type from the source permanent onto the creature that caused an
 * enter-the-battlefield trigger.
 *
 * <p>This is a non-targeting trigger marker: the enter-trigger collector binds the entering
 * permanent to the stack entry, and resolution uses the source and bound permanent ids.</p>
 *
 * @param counterType the kind of counter moved
 */
public record MoveCounterFromSourceToEnteringCreatureEffect(CounterType counterType) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
