package com.github.laxika.magicalvibes.model.effect;

/**
 * "For each player, search that player's library for a nonland card and exile it, then that player
 * shuffles. You may cast those cards without paying their mana costs." (Jace, Architect of
 * Thought's −8.)
 *
 * <p>Non-targeting. The controller searches every library in turn (APNAP order), exiling one
 * nonland card from each; the per-player searches ride the shared library-search interaction
 * pipeline with {@code PendingEachPlayerLibraryExile} carrying the remainder. Once every library
 * has been searched the accumulated exiled cards are offered through the shared free-cast queue.
 */
public record SearchEachPlayerLibraryForNonlandCardToExileAndCastEffect() implements CardEffect {
}
