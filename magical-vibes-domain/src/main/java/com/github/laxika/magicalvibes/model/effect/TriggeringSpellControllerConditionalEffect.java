package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger-only wrapper that fires the wrapped effect only when the spell or ability that caused
 * the trigger is controlled by this permanent's controller.
 */
public record TriggeringSpellControllerConditionalEffect(CardEffect wrapped) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
