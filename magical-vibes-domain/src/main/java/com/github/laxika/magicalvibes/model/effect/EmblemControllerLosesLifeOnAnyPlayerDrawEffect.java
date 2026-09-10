package com.github.laxika.magicalvibes.model.effect;

/**
 * Emblem marker for "Whenever a player draws a card, you lose N life."
 * The draw service creates a triggered ability for the emblem controller.
 */
public record EmblemControllerLosesLifeOnAnyPlayerDrawEffect(int amount) implements CardEffect {
}
