package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Mills cards from the controller's library, then offers each matching card milled by this
 * resolution for return to its owner's hand, up to {@code maxCount} cards. The offers are represented by
 * {@link ReturnMilledPermanentToHandEffect} marker effects.
 */
public record MillControllerAndMayReturnMilledPermanentToHandEffect(int count, CardPredicate filter, int maxCount)
        implements CardEffect {

    public MillControllerAndMayReturnMilledPermanentToHandEffect(int count, CardPredicate filter) {
        this(count, filter, 1);
    }

    public MillControllerAndMayReturnMilledPermanentToHandEffect(int count) {
        this(count, new CardIsPermanentPredicate(), 1);
    }

    public MillControllerAndMayReturnMilledPermanentToHandEffect(int count, int maxCount) {
        this(count, new CardIsPermanentPredicate(), maxCount);
    }

    public MillControllerAndMayReturnMilledPermanentToHandEffect {
        if (maxCount < 1) {
            throw new IllegalArgumentException("maxCount must be positive");
        }
    }
}
