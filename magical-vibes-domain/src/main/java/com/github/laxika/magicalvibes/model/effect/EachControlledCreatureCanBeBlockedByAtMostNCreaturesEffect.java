package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Team-wide static evasion restriction.
 * Each creature the source's controller controls that matches the optional predicate can't be blocked
 * by more than {@code maxBlockers} creatures.
 * The team-wide counterpart of {@link CanBeBlockedByAtMostNCreaturesEffect} (which is self-only).
 * Yuan Shao, the Indecisive uses {@code maxBlockers == 1} with no predicate.
 */
public record EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect(
        int maxBlockers,
        PermanentPredicate affectedCreaturePredicate
) implements CardEffect {

    public EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect(int maxBlockers) {
        this(maxBlockers, null);
    }
}
