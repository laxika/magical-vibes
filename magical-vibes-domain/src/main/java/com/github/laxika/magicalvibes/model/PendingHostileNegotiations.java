package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.UUID;

/**
 * Carries the two face-down piles while Hostile Negotiations waits for its two player choices.
 */
public record PendingHostileNegotiations(UUID controllerId, UUID opponentId,
                                         List<Card> pile1Cards, List<Card> pile2Cards)
        implements PendingInteraction {

    public PendingHostileNegotiations {
        pile1Cards = List.copyOf(pile1Cards);
        pile2Cards = List.copyOf(pile2Cards);
    }
}
