package com.github.laxika.magicalvibes.model.effect;

/**
 * On resolution, pays all available mana from the controller's mana pool as X
 * and gains X life. Used for "you may pay {X}. If you do, you gain X life"
 * triggered abilities where the payment decision is made during resolution
 * (e.g. Vigil for the Lost).
 */
public record PayXManaGainXLifeEffect() implements CardEffect {
}
