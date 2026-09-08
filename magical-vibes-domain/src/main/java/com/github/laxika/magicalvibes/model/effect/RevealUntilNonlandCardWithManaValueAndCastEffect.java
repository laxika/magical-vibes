package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals cards from the top of the controller's library until a nonland card with a mana value
 * at or below the configured maximum is found, then offers that card for a free cast.
 */
public record RevealUntilNonlandCardWithManaValueAndCastEffect(int maxManaValue) implements CardEffect {
}
