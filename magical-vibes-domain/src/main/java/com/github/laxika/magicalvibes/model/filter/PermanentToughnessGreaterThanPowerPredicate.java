package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose effective toughness is greater than their effective power.
 */
public record PermanentToughnessGreaterThanPowerPredicate() implements PermanentPredicate {
}
