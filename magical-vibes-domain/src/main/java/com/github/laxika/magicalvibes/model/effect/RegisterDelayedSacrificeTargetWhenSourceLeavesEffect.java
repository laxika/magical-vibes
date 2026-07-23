package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, registers a delayed triggered ability for the rest of the turn:
 * "When this creature leaves the battlefield this turn, sacrifice that creature."
 *
 * <p>Reads the shared creature target and the ability's source permanent from the stack entry.
 * Used by Phantasmal Mount (paired with {@link BoostTargetCreatureEffect} and
 * {@link RegisterDelayedSacrificeSourceWhenTargetLeavesEffect}).
 */
public record RegisterDelayedSacrificeTargetWhenSourceLeavesEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE);
    }
}
