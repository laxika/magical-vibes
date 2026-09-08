package com.github.laxika.magicalvibes.model.effect;

/**
 * Death-trigger-only condition for wording that refers to whether the permanent was a creature
 * as it died. The death-trigger collector consumes this wrapper using the event's snapshotted
 * creature status before putting the wrapped effect on the stack.
 */
public record DyingPermanentWasCreatureConditionalEffect(CardEffect wrapped) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
