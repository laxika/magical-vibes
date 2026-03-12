package com.github.laxika.magicalvibes.model.effect;

/**
 * Boosts all creatures you control by +X/+X until end of turn, where X is the greatest
 * power among creatures you control at the time this effect resolves.
 * Used by Overwhelming Stampede.
 */
public record BoostAllOwnCreaturesByGreatestPowerEffect() implements CardEffect {
}
