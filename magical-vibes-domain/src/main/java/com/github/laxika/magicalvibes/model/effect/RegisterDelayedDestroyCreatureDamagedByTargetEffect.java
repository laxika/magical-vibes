package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, registers a delayed triggered ability for the rest of the turn:
 * "Whenever target creature deals combat damage to a non-Wall creature this turn, destroy that
 * non-Wall creature."
 *
 * <p>Reads the shared creature target from the stack entry. Not a {@code RemovalEffect} — nothing
 * happens to the targeted creature itself; the destruction hits whatever it damages. Used by Acidic
 * Dagger (paired with {@link RegisterDelayedSacrificeSourceWhenTargetLeavesEffect}).
 */
public record RegisterDelayedDestroyCreatureDamagedByTargetEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
