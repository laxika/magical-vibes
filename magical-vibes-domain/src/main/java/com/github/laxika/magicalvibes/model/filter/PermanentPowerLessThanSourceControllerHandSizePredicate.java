package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose effective power is less than the source controller's current hand size.
 */
public record PermanentPowerLessThanSourceControllerHandSizePredicate() implements PermanentPredicate {
}
