package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroy target permanent. This creature gets +X/+0 until end of turn, where X is that permanent's mana value.
 * The boost is applied regardless of whether the destruction succeeds (e.g. indestructible).
 * The target type restriction (artifact, creature, etc.) is handled by the ability's target filter.
 * Used by Hoard-Smelter Dragon.
 */
public record DestroyTargetPermanentAndBoostSelfByManaValueEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
