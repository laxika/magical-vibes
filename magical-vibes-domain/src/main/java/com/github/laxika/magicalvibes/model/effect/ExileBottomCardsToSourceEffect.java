package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/** Exiles cards from the bottom of the controller's library and tracks them with the source permanent. */
public record ExileBottomCardsToSourceEffect(DynamicAmount count) implements CardEffect {
}
