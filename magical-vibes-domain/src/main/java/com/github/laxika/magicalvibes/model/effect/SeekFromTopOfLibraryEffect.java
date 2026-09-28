package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Randomly puts one matching card from the top of the controller's library into their hand,
 * then shuffles the library.
 *
 * @param count the maximum number of cards considered from the top of the library
 * @param predicate the cards eligible to be sought
 */
public record SeekFromTopOfLibraryEffect(int count, CardPredicate predicate) implements CardEffect {
}
