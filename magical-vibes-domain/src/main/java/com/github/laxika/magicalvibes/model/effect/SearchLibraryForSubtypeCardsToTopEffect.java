package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Search your library for any number of cards of {@code subtype}, reveal them, then shuffle and
 * put those cards on top in any order.
 *
 * <p>Unlike {@link SearchLibraryEffect} with {@code TOP_OF_LIBRARY} (which puts a single card on
 * top), this puts an arbitrary subset on top: the controller picks any number of the matching
 * cards, the library is shuffled, and the chosen cards are placed on top in an order the
 * controller chooses. Used by Goblin Recruiter (Goblin).
 */
public record SearchLibraryForSubtypeCardsToTopEffect(CardSubtype subtype) implements CardEffect {
}
