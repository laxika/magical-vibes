package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Randomly seeks a card from the controller's library whose mana value is greater than the
 * evaluated reference value, exiles it, and lets the controller play it until the end of their
 * next turn.
 */
public record SeekFromLibraryWithGreaterManaValueEffect(DynamicAmount referenceManaValue)
        implements CardEffect {
}
