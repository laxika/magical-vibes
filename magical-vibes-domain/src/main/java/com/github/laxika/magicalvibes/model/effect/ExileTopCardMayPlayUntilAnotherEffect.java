package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of the controller's library and lets them play it until the source
 * permanent exiles another card with this effect.
 */
public record ExileTopCardMayPlayUntilAnotherEffect() implements CardEffect {
}
