package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that gives each non-Human creature the controller controls +1/+1 for each of
 * its creature types, up to the configured maximum.
 */
public record BoostNonHumanCreaturesByCreatureTypeCountEffect(int maximum) implements CardEffect {
}
