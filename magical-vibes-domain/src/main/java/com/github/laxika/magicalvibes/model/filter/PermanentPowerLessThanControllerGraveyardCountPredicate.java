package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose effective power is less than the number of cards in the source
 * controller's graveyard, evaluated from the current game state.
 */
public record PermanentPowerLessThanControllerGraveyardCountPredicate() implements PermanentPredicate {
}
