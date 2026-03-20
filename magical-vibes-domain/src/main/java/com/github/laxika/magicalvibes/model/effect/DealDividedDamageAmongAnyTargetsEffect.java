package com.github.laxika.magicalvibes.model.effect;

/**
 * "Deals N damage divided as you choose among any number of targets (creatures and/or players)."
 *
 * <p>Damage assignments (target UUID → amount) are stored on
 * {@code StackEntry.damageAssignments}. Unlike
 * {@link DealDividedDamageAmongTargetCreaturesEffect} which only targets creatures,
 * this effect can target both permanents and players.
 *
 * <p>Used by kicked spells like Fight with Fire (10 damage divided among any targets when kicked).
 */
public record DealDividedDamageAmongAnyTargetsEffect(int totalDamage) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public boolean canTargetPlayer() {
        return true;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
