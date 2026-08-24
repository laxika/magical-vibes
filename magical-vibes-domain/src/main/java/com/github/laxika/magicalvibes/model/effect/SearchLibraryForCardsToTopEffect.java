package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Search your library for any number of cards matching {@code filter}, reveal them, then shuffle
 * and put the chosen cards on top in any order. The {@link #exact(DynamicAmount)} form searches
 * for a quantity of cards and puts that many, or as many as possible, on top without revealing
 * them to other players.
 *
 * <p>The matching cards are held out of the library while the controller chooses any subset. The
 * existing search-to-top interaction is reused for the choice and ordering steps.
 */
public record SearchLibraryForCardsToTopEffect(CardPredicate filter, DynamicAmount count,
                                               boolean revealCards) implements CardEffect {

    public SearchLibraryForCardsToTopEffect(CardPredicate filter) {
        this(filter, null, true);
    }

    /** Search for exactly {@code count} cards, without revealing them to other players. */
    public static SearchLibraryForCardsToTopEffect exact(DynamicAmount count) {
        return new SearchLibraryForCardsToTopEffect(null, count, false);
    }
}
