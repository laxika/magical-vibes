package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals cards from the top of the controller's library until two nonland cards with mana value
 * at or below the configured maximum are found, then offers one for a free cast.
 */
public record RevealUntilTwoNonlandCardsWithManaValueAndCastOneEffect(int maxManaValue)
        implements CardEffect {
}
