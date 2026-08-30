package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals cards from the top of the controller's library until a card matching the predicate is
 * found, offers that card to be cast without paying its mana cost, and puts the other revealed
 * cards on the bottom of the library in a random order.
 */
public record RevealUntilCardPredicateMayCastWithoutPayingManaEffect(CardPredicate predicate)
        implements CardEffect {
}
