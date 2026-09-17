package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a creature if it has the least power among creatures controlled by that creature's
 * controller. Multiple creatures can match if tied for least power.
 * Requires game data to evaluate.
 */
public record PermanentHasLeastPowerAmongControllerCreaturesPredicate() implements PermanentPredicate {
}
