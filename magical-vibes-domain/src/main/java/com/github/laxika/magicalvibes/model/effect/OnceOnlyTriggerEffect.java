package com.github.laxika.magicalvibes.model.effect;

/** Wrapper for a triggered ability that can trigger only once for a permanent object. */
public record OnceOnlyTriggerEffect(CardEffect wrapped) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
