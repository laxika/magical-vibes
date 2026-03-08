package com.github.laxika.magicalvibes.model.effect;

/**
 * Adds {C} for each charge counter on the source permanent.
 * Used by cards like Shrine of Boundless Growth.
 * Implements ManaProducingEffect so the engine treats the ability as a mana ability (CR 605.1a).
 */
public record AddColorlessManaPerChargeCounterOnSourceEffect() implements ManaProducingEffect {
}
