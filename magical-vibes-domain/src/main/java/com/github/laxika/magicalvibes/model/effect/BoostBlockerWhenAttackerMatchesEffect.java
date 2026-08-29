package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Trigger marker for effects such as "whenever a creature blocks a [filtered] creature, the
 * blocking creature gets +X/+Y until end of turn".
 *
 * <p>It is collected from {@code ON_ANY_CREATURE_BECOMES_BLOCKED} once for each matching
 * attacker/blocker pair and materializes a non-targeting {@link BoostTargetCreatureEffect} aimed
 * at the blocker.
 */
public record BoostBlockerWhenAttackerMatchesEffect(
        PermanentPredicate attackerPredicate,
        int powerBoost,
        int toughnessBoost
) implements BlockPairConditionalEffect {

    @Override
    public BlockParticipant actsOn() {
        return BlockParticipant.BLOCKER;
    }

    @Override
    public boolean firesForPair(int attackerPower, int blockerPower) {
        return true;
    }

    @Override
    public CardEffect resolvedEffect() {
        return new BoostTargetCreatureEffect(powerBoost, toughnessBoost);
    }
}
