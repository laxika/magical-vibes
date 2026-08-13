package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose effective power is greater than the active player's hand size.
 * Evaluation uses the current active player and hand at resolution time.
 */
public record PermanentPowerGreaterThanActivePlayerHandSizePredicate() implements PermanentPredicate {
}
