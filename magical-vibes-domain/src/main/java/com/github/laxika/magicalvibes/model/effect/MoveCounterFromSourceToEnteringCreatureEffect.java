package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Moves one counter of the given type from the source permanent onto "that creature": the stack entry's
 * triggering permanent, read as the ability resolves, never a target. Graft's "you may move a +1/+1 counter from
 * this permanent onto that creature" is a {@link MayEffect} around it behind the intervening "if" "if this
 * permanent has a +1/+1 counter on it".
 *
 * @param counterType the kind of counter moved
 */
public record MoveCounterFromSourceToEnteringCreatureEffect(CounterType counterType) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
