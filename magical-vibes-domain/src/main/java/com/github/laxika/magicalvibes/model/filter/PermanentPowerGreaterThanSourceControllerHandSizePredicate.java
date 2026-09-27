package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose effective power is greater than the source controller's current hand size.
 */
public record PermanentPowerGreaterThanSourceControllerHandSizePredicate() implements PermanentPredicate {
}
