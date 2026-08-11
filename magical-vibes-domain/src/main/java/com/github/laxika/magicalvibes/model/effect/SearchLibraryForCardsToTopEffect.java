package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Search your library for any number of cards matching {@code filter}, reveal them, then shuffle
 * and put the chosen cards on top in any order.
 *
 * <p>The matching cards are held out of the library while the controller chooses any subset. The
 * existing search-to-top interaction is reused for the choice and ordering steps.
 */
public record SearchLibraryForCardsToTopEffect(CardPredicate filter) implements CardEffect {
}
