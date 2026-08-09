package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals the top card of the controller's library. If it matches the given predicate, it is put
 * into the controller's hand; otherwise it is put into the controller's graveyard.
 */
public record RevealTopCardMatchingToHandElseGraveyardEffect(CardPredicate matchPredicate)
        implements CardEffect {
}
