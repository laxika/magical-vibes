package com.github.laxika.magicalvibes.model.effect;

/**
 * Secrets of Strixhaven "Prepared": the target creature becomes prepared.
 * <p>
 * On resolution, a copy of the target's prepare spell (its {@code backFaceCard}) is created in
 * exile with a non-expiring play permission for the target's controller, and the target permanent
 * is marked prepared. A no-op if the target is already prepared, has no prepare spell, or is not
 * a creature.
 */
public record MakeTargetCreaturePreparedEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
