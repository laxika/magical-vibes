package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches creatures whose effective power is at most the number of creatures
 * the source's controller controls. Evaluated using {@link FilterContext#sourceControllerId()}
 * and {@link FilterContext#gameData()} at evaluation time.
 * Used for abilities like "target creature with power less than or equal to the number of creatures you control"
 * (Beguiler of Wills).
 */
public record PermanentPowerAtMostControlledCreatureCountPredicate() implements PermanentPredicate {
}
