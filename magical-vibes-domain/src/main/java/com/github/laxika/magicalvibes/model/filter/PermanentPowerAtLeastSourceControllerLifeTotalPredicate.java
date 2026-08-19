package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose effective power is at least the life total of the source's controller.
 * The controller and current life total are read from {@link FilterContext} at evaluation time.
 */
public record PermanentPowerAtLeastSourceControllerLifeTotalPredicate() implements PermanentPredicate {
}
