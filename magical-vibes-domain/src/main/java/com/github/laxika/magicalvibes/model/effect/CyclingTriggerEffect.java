package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger-only wrapper for an effect that fires when the controller cycles a card.
 *
 * <p>This is kept distinct from the ordinary controller-discard trigger because cycling is a
 * discard, but not every discard is cycling.</p>
 * <p>
 * Not authored on a card: the legacy view ({@code TriggeredAbility.legacyEffects()}) builds it for the collectors
 * from {@code Triggers.youCycleCard()}.
 */
public record CyclingTriggerEffect(CardEffect wrapped) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
