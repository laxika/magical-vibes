package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals the top {@code count} cards, then offers any number of matching cards with distinct
 * powers for the controller to put into their hand. The remaining revealed cards go to the bottom
 * of the library in a random order.
 */
public record RevealTopCardsWithDifferentPowersToHandEffect(DynamicAmount count, CardPredicate predicate)
        implements CardEffect {
}
