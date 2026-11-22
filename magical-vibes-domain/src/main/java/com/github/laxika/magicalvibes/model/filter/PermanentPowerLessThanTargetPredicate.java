package com.github.laxika.magicalvibes.model.filter;

/**
 * "With lesser power" after "target creature": power less than the entry's target permanent has as the effect
 * resolves, or last had (CR 608.2h). Only a {@code controlledBy} set of a resolving effect reads it: the subject
 * resolver binds it to that power.
 */
public record PermanentPowerLessThanTargetPredicate() implements PermanentPredicate {
}
