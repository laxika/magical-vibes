package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals X damage divided evenly (rounded down) among any number of targets (creatures and/or players).
 * Uses {@code entry.getXValue()} for the X value and {@code entry.getTargetIds()} for the target list.
 * Each target receives {@code floor(X / numberOfTargets)} damage.
 * Used by Fireball.
 */
public record DealXDamageDividedEvenlyAmongTargetsEffect() implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
