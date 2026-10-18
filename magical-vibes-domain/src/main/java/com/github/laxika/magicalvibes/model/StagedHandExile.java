package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A card a player chose to exile from their hand during an each-player exile, held out of the hand
 * until every player has chosen so that all chosen cards are exiled together (CR 101.4).
 * {@code choice} is the prompt it was chosen from and carries the exile's parameters.
 */
public record StagedHandExile(UUID playerId, Card card, PendingInteraction.ExileFromHandChoice choice) {
}
