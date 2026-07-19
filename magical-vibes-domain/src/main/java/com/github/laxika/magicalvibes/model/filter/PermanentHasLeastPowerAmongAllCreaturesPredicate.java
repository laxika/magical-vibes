package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent if it is a creature with the least power among all creatures on the
 * battlefield (across every player's battlefield). Multiple creatures can match if tied for
 * least power. Requires game data to evaluate. Used by Wretched Banquet.
 */
public record PermanentHasLeastPowerAmongAllCreaturesPredicate() implements PermanentPredicate {
}
