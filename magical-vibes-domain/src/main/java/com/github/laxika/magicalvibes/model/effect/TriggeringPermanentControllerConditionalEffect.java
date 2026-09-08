package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger-only wrapper for effects that fire when the triggering permanent was controlled by the
 * watcher.
 */
public record TriggeringPermanentControllerConditionalEffect(CardEffect wrapped) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
