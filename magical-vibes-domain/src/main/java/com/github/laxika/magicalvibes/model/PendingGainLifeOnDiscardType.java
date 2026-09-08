package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Tracks life to gain after a discard interaction completes, based on the types of cards discarded
 * during that interaction.
 */
public record PendingGainLifeOnDiscardType(Card sourceCard, StackEntryType sourceEntryType,
                                           UUID controllerId, CardType requiredType,
                                           int lifePerCard, int matchingCardCount) {

    public PendingGainLifeOnDiscardType withMatchingCard() {
        return new PendingGainLifeOnDiscardType(sourceCard, sourceEntryType, controllerId,
                requiredType, lifePerCard, matchingCardCount + 1);
    }
}
