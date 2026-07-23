package com.github.laxika.magicalvibes.model.filter;

/**
 * Permanents controlled by the active player ({@code GameData.activePlayerId}). Used by Norritt's
 * "target … creature the active player [controls]".
 */
public record PermanentControlledByActivePlayerPredicate() implements PermanentPredicate {
}
