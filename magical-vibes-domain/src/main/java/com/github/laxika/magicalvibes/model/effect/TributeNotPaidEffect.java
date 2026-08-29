package com.github.laxika.magicalvibes.model.effect;

/**
 * Wraps the triggered ability that checks whether tribute was paid. The ETB resolver unwraps this
 * effect only when the entering permanent did not receive the required tribute counters.
 */
public record TributeNotPaidEffect(CardEffect wrapped) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
