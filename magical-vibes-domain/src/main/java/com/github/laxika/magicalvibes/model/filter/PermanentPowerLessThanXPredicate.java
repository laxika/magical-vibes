package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches creatures whose effective power is strictly less than X, where X comes from the
 * {@link FilterContext#xValue()} at evaluation time. Used for abilities whose source is sacrificed
 * as a cost and whose power is snapshotted into X at payment ("creatures you control with power
 * less than this creature's power" — Lena, Selfless Champion).
 */
public record PermanentPowerLessThanXPredicate() implements PermanentPredicate {
}
