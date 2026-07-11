package com.github.laxika.magicalvibes.model.effect;

/**
 * Team-wide static evasion restriction.
 * Each creature the source's controller controls can't be blocked by more than {@code maxBlockers} creatures.
 * The team-wide counterpart of {@link CanBeBlockedByAtMostNCreaturesEffect} (which is self-only).
 * Yuan Shao, the Indecisive uses {@code maxBlockers == 1}.
 */
public record EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect(int maxBlockers) implements CardEffect {
}
