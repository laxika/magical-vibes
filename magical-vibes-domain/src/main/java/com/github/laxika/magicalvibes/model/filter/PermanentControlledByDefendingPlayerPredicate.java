package com.github.laxika.magicalvibes.model.filter;

/**
 * Permanents controlled by a defending player of the current combat — a player who is being attacked
 * directly or through a planeswalker they control. In a two-player game the nonactive player is the
 * defending player for the whole combat phase, even before attackers are declared or after the last
 * attacker has left combat (CR 506.2). Matches nothing outside the combat phase. Used by Yare's
 * "target creature defending player controls".
 */
public record PermanentControlledByDefendingPlayerPredicate() implements PermanentPredicate {
}
