package com.github.laxika.magicalvibes.model.effect;

/**
 * Static evasion restriction on attackers.
 * This creature can't be blocked by more than N creatures.
 */
public record CanBeBlockedByAtMostNCreaturesEffect(int maxBlockers) implements CardEffect {
}
