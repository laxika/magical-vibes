package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Context stored on GameData before a library search with destination EXILE.
 * After the search completes and the card is exiled, the opponent is asked
 * whether to let the controller have the exiled card. If the opponent declines,
 * the controller draws {@code drawCountOnDecline} cards instead.
 */
public record PendingOpponentExileChoice(UUID controllerId, int drawCountOnDecline) {
}
