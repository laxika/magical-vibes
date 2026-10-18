package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches an attacking creature (attacking a player, planeswalker, or battle) while the source controller is tied for the highest life total
 * among all players.
 */
public record PermanentAttacksWhileSourceControllerHasMostLifePredicate() implements PermanentPredicate {
}
