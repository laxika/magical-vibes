package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a player who doesn't have max speed ("each player who doesn't have max speed", Outpace Oblivion): a player
 * has max speed if their speed is 4 (CR 702.179e); a player with no speed doesn't have it.
 */
public record PlayerWithoutMaxSpeedPredicate() implements PlayerPredicate {
}
