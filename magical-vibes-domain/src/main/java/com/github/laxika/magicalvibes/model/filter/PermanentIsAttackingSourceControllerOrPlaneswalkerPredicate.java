package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches creatures attacking the source controller directly or attacking one of that player's
 * planeswalkers. Requires a {@link FilterContext} with the source controller and game data.
 */
public record PermanentIsAttackingSourceControllerOrPlaneswalkerPredicate() implements PermanentPredicate {
}
