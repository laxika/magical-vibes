package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Mills cards from the controller's library, then offers each matching card milled by this
 * resolution for return to its owner's hand. The offers are represented by
 * {@link ReturnMilledPermanentToHandEffect} marker effects.
 */
public record MillControllerAndMayReturnMilledPermanentToHandEffect(int count, CardPredicate filter)
        implements CardEffect {

    public MillControllerAndMayReturnMilledPermanentToHandEffect(int count) {
        this(count, new CardIsPermanentPredicate());
    }
}
