package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.UUID;

/**
 * Queued entry for "each player returns up to N cards from graveyard to battlefield" prompts
 * when multiple players must choose cards sequentially (e.g. Fall of the Thran chapters II/III).
 */
public record PendingGraveyardReturnChoice(UUID playerId, int remainingCount, CardPredicate filter) {
}
