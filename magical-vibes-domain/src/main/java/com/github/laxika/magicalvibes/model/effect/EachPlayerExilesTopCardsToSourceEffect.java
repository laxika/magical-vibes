package com.github.laxika.magicalvibes.model.effect;

/**
 * ETB effect: each player exiles the top N cards of their library,
 * tracked as "exiled with" the source permanent (e.g. Knowledge Pool).
 */
public record EachPlayerExilesTopCardsToSourceEffect(int count) implements CardEffect {
}
