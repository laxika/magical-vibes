package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals the top card of the controller's library. If it matches the given predicate, it is put
 * into the controller's hand; otherwise, the controller may put it on the bottom of their library.
 */
public record RevealTopCardMatchingToHandElseMayBottomEffect(CardPredicate matchPredicate)
        implements CardEffect {
}
