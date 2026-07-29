package com.github.laxika.magicalvibes.model.filter;

/**
 * Permanents controlled by a defending player of the current combat — a player who is being attacked
 * directly or through a planeswalker they control. Matches nothing outside combat or before attackers
 * are declared. Used by Yare's "target creature defending player controls".
 */
public record PermanentControlledByDefendingPlayerPredicate() implements PermanentPredicate {
}
