package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger-only wrapper for effects that fire when the triggering spell was controlled by the
 * source permanent's controller.
 */
public record TriggeringSpellControllerConditionalEffect(CardEffect wrapped) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
