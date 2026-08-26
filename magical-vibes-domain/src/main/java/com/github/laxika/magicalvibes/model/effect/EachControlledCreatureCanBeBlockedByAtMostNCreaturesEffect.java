package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Team-wide static evasion restriction.
 * Each creature the source's controller controls, optionally matching
 * {@code affectedCreatureFilter}, can't be blocked by more than {@code maxBlockers} creatures.
 * The team-wide counterpart of {@link CanBeBlockedByAtMostNCreaturesEffect} (which is self-only).
 * Yuan Shao, the Indecisive uses {@code maxBlockers == 1}. In a spell slot, its normal-effect
 * handler snapshots the controller's creatures and applies the per-creature counterpart until
 * end of turn. Challenger Troll uses a power filter.
 */
public record EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect(
        int maxBlockers,
        PermanentPredicate affectedCreatureFilter
) implements CardEffect {

    public EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect(int maxBlockers) {
        this(maxBlockers, null);
    }
}
