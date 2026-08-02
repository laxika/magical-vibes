package com.github.laxika.magicalvibes.model.effect;

/**
 * Put the top {@code count} cards of your library on the bottom of your library in any order
 * (Petals of Insight's "You may put those cards on the bottom of your library in any order").
 * The controller orders them through a {@code LibraryReorder} interaction; a shorter library just
 * moves the cards it has.
 *
 * @param count how many cards to move from the top to the bottom
 */
public record PutTopCardsOfLibraryOnBottomEffect(int count) implements CardEffect {
}
