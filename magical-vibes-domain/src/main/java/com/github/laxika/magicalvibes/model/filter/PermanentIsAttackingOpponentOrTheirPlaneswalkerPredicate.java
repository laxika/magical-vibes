package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches creatures attacking one of the source controller's opponents directly or attacking a
 * planeswalker controlled by one of those opponents. Requires game data and a source controller.
 */
public record PermanentIsAttackingOpponentOrTheirPlaneswalkerPredicate() implements PermanentPredicate {
}
