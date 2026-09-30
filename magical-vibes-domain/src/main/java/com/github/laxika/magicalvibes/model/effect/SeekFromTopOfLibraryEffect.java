package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Randomly puts up to {@code maxMatches} matching cards from the top of the controller's library
 * into their hand, then shuffles the library.
 *
 * @param count the maximum number of cards considered from the top of the library
 * @param maxMatches the maximum number of matching cards to seek
 * @param predicate the cards eligible to be sought
 */
public record SeekFromTopOfLibraryEffect(int count, int maxMatches, CardPredicate predicate)
        implements CardEffect {

    public SeekFromTopOfLibraryEffect(int count, CardPredicate predicate) {
        this(count, 1, predicate);
    }
}
