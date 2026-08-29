package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches an attacking creature whose direct player attack target is tied for the highest life
 * total among all players.
 */
public record PermanentAttacksPlayerWithMostLifePredicate() implements PermanentPredicate {
}
