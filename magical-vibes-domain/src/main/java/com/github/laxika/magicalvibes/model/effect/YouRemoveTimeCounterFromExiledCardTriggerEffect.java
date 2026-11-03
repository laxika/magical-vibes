package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for an {@code Triggers.timeCounterRemovedFromThisInExile()} ability that fires only when the
 * card's controller is the player removing the time counter ("Whenever you remove a time counter
 * from this card while it's exiled" — Watcher of Hours). Don't author it: the trigger
 * {@code Triggers.youRemoveTimeCounterFromThisInExile()} says this, and its legacy view
 * ({@code TriggeredAbility.legacyEffects()}) gives it to the exiled time-counter collector.
 */
public record YouRemoveTimeCounterFromExiledCardTriggerEffect(CardEffect wrapped) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
