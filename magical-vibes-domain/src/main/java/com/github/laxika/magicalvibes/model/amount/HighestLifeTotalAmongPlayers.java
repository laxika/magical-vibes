package com.github.laxika.magicalvibes.model.amount;

/**
 * The highest life total among <em>all</em> players, the controller included (Arbiter of
 * Knollridge: "each player's life total becomes the highest life total among all players").
 * The controller-excluding sibling is {@link HighestOpponentLifeTotal}.
 */
public record HighestLifeTotalAmongPlayers() implements DynamicAmount {
}
