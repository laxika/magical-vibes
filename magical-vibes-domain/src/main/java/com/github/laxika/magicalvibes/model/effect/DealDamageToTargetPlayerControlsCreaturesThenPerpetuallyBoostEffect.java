package com.github.laxika.magicalvibes.model.effect;

/** Deals damage to each creature controlled by a target player, then perpetually boosts those creatures. */
public record DealDamageToTargetPlayerControlsCreaturesThenPerpetuallyBoostEffect(
        int damage, int powerBoost, int toughnessBoost) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
