package com.github.laxika.magicalvibes.model.effect;

/**
 * Exile all nontoken permanents. Starting with the controller, each player (in turn order,
 * wrapping) chooses one of the exiled cards and puts it onto the battlefield tapped under their
 * control. Repeat until every card exiled this way has been chosen.
 * <p>
 * Used by Thieves' Auction. Requires repeated player interaction (a shared auction pool).
 */
public record PermanentAuctionEffect() implements CardEffect {
}
