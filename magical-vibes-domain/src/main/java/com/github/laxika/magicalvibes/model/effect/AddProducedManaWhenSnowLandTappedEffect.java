package com.github.laxika.magicalvibes.model.effect;

/**
 * Land-tap trigger: whenever any player taps a snow land for mana, that player adds one mana
 * of any type that land produced. Symmetric — fires for every player's snow lands.
 * Used by Winter's Night.
 *
 * <p>Like {@link AddOneOfEachManaTypeProducedByLandEffect}, when the land produces multiple
 * types this adds one mana of the first type it produces.</p>
 */
public record AddProducedManaWhenSnowLandTappedEffect() implements CardEffect {
}
