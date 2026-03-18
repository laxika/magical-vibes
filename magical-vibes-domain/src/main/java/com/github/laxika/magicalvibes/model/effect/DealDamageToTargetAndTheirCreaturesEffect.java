package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals a fixed amount of damage to target player or planeswalker AND each creature
 * that player or that planeswalker's controller controls.
 * Used by Chandra Nalaar's ultimate ability.
 */
public record DealDamageToTargetAndTheirCreaturesEffect(int damage) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
