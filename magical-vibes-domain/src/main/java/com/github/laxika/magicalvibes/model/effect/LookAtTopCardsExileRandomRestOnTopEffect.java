package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: "Look at the top {@code count} cards of your library. Exile {@code exileCount}
 * of them at random, then put the rest on top of your library in any order."
 *
 * <p>Used by Orcish Librarian ({@code count = 8}, {@code exileCount = 4}). The exiles are chosen by
 * the engine, not the player; the surviving cards are ordered through an asynchronous
 * {@code PendingInteraction.LibraryReorder} when two or more remain.
 *
 * @param count      how many cards are looked at from the top of the controller's library
 * @param exileCount how many of those are exiled at random
 */
public record LookAtTopCardsExileRandomRestOnTopEffect(int count, int exileCount) implements CardEffect {
}
