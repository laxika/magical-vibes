package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches the controller's library for a creature card with mana value one greater than the
 * permanent sacrificed by the same resolving ability and puts it onto the battlefield.
 */
public record SearchLibraryForCreatureWithOneMoreManaValueThanSacrificedPermanentEffect()
        implements CardEffect {
}
