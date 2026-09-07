package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants the controller permission to look at and play cards from the top of their library until
 * end of turn. The permission covers both casting spells and playing lands.
 */
public record AllowPlayFromTopOfLibraryUntilEndOfTurnEffect() implements CardEffect {
}
