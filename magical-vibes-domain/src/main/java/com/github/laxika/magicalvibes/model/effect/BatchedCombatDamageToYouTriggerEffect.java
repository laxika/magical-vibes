package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker wrapper for an {@code Triggers.creatureDealsCombatDamageToYou()} ability that triggers once
 * for a combat-damage step containing one or more qualifying creatures.
 */
public record BatchedCombatDamageToYouTriggerEffect(CardEffect wrapped) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
