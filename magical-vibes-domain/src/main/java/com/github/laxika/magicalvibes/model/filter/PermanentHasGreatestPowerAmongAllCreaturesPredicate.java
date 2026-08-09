package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent if it is a creature with the greatest effective power among all creatures
 * on the battlefield. Multiple creatures can match if tied for greatest power.
 * Requires game data to evaluate.
 */
public record PermanentHasGreatestPowerAmongAllCreaturesPredicate() implements PermanentPredicate {
}
