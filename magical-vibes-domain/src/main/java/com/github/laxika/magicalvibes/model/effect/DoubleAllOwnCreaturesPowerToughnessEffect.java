package com.github.laxika.magicalvibes.model.effect;

/**
 * Doubles the power and toughness of each creature the controller controls until end of turn.
 * <p>
 * Per CR 701.9a/b, each creature gets +X/+Y where X and Y are that creature's power and toughness
 * as this ability resolves (evaluated independently per creature).
 */
public record DoubleAllOwnCreaturesPowerToughnessEffect() implements CardEffect {
}
