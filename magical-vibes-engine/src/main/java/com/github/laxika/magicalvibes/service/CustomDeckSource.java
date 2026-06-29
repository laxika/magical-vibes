package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

/**
 * Engine-side abstraction over user-built (custom) decks, which are loaded from the application's
 * persistence layer. The engine itself only knows how to build {@code PrebuiltDeck}s; any module
 * that can resolve a custom deck id (e.g. the backend {@code DeckService}) provides an
 * implementation. {@link GameSetupService} consumes it optionally, so headless/AI/test contexts
 * that only use prebuilt decks need not supply one.
 */
public interface CustomDeckSource {

    boolean isCustomDeck(String deckId);

    List<Card> buildCustomDeck(String deckId);
}
