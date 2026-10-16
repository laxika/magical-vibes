package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Searches the controller's library and/or graveyard for one matching card to put into hand.
 *
 * @param filter             the card the search looks for
 * @param shuffleAfterSearch whether the search ends with the library being shuffled; a search that is
 *                           one of several instructions in the same effect (Journey for the Elixir) sets
 *                           this only on the last one, so the library is shuffled once (CR 701.23h)
 */
public record SearchLibraryAndOrGraveyardForCardToHandEffect(CardPredicate filter,
                                                             boolean shuffleAfterSearch) implements CardEffect {

    public SearchLibraryAndOrGraveyardForCardToHandEffect(CardPredicate filter) {
        this(filter, true);
    }
}
