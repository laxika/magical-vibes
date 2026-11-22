package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a player who controls the most creatures, a player tied for the most included ("each player who controls the
 * most creatures investigates", No Witnesses).
 */
public record PlayerControlsMostCreaturesPredicate() implements PlayerPredicate {
}
