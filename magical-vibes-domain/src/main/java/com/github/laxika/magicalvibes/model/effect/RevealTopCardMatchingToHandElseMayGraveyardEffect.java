package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals the top card of the controller's library. If it matches the given predicate, it is put
 * into the controller's hand; otherwise, the controller may put it into their graveyard.
 */
public record RevealTopCardMatchingToHandElseMayGraveyardEffect(CardPredicate matchPredicate)
        implements CardEffect {
}
