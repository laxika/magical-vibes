package com.github.laxika.magicalvibes.model.effect;

/**
 * Sacrifice another creature, then draw cards equal to its mana value and offer a permanent card
 * with mana value up to that value from the controller's hand to the battlefield.
 *
 * <p>The effect is intended to be wrapped in {@link MayEffect} when the sacrifice is optional.
 */
public record SacrificeAnotherCreatureDrawAndMayPutPermanentEffect() implements CardEffect {
}
