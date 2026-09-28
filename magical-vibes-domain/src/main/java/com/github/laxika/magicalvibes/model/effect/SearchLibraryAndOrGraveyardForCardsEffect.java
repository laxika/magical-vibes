package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Searches the controller's library and/or graveyard for up to a number of matching cards. */
public record SearchLibraryAndOrGraveyardForCardsEffect(
        CardPredicate filter,
        int maxCount,
        LibrarySearchDestination destination) implements CardEffect {

    public SearchLibraryAndOrGraveyardForCardsEffect(CardPredicate filter, int maxCount) {
        this(filter, maxCount, LibrarySearchDestination.HAND);
    }

    public SearchLibraryAndOrGraveyardForCardsEffect {
        if (maxCount < 1) {
            throw new IllegalArgumentException("maxCount must be positive");
        }
        if (destination != LibrarySearchDestination.HAND
                && destination != LibrarySearchDestination.BATTLEFIELD) {
            throw new IllegalArgumentException("Unsupported combined search destination: " + destination);
        }
    }
}
