package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals X damage to each of multiple targets (creatures and/or players).
 * Each target receives the full X damage (not divided).
 * Uses {@code entry.getXValue()} for the damage and {@code entry.getTargetIds()} for targets.
 * Used by Jaya's Immolating Inferno.
 */
public record DealXDamageToEachTargetEffect() implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
