package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroy target permanent. You gain life equal to its mana value.
 * Life gain occurs regardless of whether the destruction succeeds (e.g. indestructible).
 * The target type restriction (artifact, creature, etc.) is handled by the spell's target filter.
 * Used by Divine Offering.
 */
public record DestroyTargetPermanentAndGainLifeEqualToManaValueEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
