package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent if it is a creature controlled by the source's controller
 * and has the greatest power among all creatures that player controls.
 * Multiple creatures can match if tied for greatest power.
 * Requires {@link FilterContext} with gameData and sourceControllerId.
 */
public record PermanentHasGreatestPowerAmongControlledCreaturesPredicate() implements PermanentPredicate {
}
