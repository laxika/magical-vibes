package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Exiles the top {@code count} cards of the controller's library, then applies the riders for
 * land, blue, and red cards among those exiled cards.
 */
public record ExileTopCardsAndApplyColorRidersEffect(DynamicAmount count) implements CardEffect {
}
