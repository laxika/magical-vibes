package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, registers a delayed triggered ability for the rest of the turn:
 * "When that creature leaves the battlefield this turn, sacrifice this creature."
 *
 * <p>Reads the shared creature target and the ability's source permanent from the stack entry.
 * Used by Kjeldoran Elite Guard (paired with {@link BoostTargetCreatureEffect}).
 */
public record RegisterDelayedSacrificeSourceWhenTargetLeavesEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE);
    }
}
