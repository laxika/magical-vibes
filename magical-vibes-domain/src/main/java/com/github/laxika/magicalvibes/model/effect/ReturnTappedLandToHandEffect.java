package com.github.laxika.magicalvibes.model.effect;

/**
 * "Whenever a land is tapped for mana, return it to its owner's hand."
 * Symmetric land-tap trigger — fires for every player's land taps.
 * Registered in {@code EffectSlot.ON_ANY_PLAYER_TAPS_LAND}. Used by Storm Cauldron.
 */
public record ReturnTappedLandToHandEffect() implements CardEffect {
}
