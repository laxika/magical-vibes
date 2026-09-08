package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger-only wrapper for an ability that fires when an opponent's spell or ability causes the
 * controller to discard a card. The wrapped effect is placed on the triggered ability's stack entry.
 */
public record OpponentCausedDiscardTriggerEffect(CardEffect wrapped) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
