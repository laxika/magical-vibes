package com.github.laxika.magicalvibes.model.effect;

/**
 * Triggered effect: target opponent exiles the top N cards of their library face down,
 * tracked as "exiled with" the source permanent (Grimoire Thief).
 *
 * <p>The opponent-library counterpart of {@link ExileTopCardsToSourceEffect} (controller's own
 * library) and {@link EachPlayerExilesTopCardsToSourceEffect} (every player). In a two-player game
 * the single opponent is the only legal target, so no separate target choice is required.
 */
public record ExileTopCardsOfOpponentLibraryToSourceEffect(int count) implements CardEffect {
}
