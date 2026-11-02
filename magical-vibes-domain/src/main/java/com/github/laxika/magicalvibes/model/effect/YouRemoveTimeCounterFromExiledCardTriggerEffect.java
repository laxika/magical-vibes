package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for an {@code Triggers.timeCounterRemovedFromThisInExile()} ability that fires only when the
 * card's controller is the player removing the time counter ("Whenever you remove a time counter
 * from this card while it's exiled" — Watcher of Hours).
 */
public record YouRemoveTimeCounterFromExiledCardTriggerEffect(CardEffect wrapped) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
