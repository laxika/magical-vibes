package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a creature if it has the greatest power among creatures controlled by that creature's
 * controller. Multiple creatures can match if tied for greatest power.
 * Requires game data to evaluate.
 */
public record PermanentHasGreatestPowerAmongControllerCreaturesPredicate() implements PermanentPredicate {
}
