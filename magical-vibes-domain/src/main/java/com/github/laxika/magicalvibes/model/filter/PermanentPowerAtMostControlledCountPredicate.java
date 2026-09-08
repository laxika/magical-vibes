package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose power is at most the number of matching permanents controlled by the
 * source's controller.
 *
 * @param countFilter filter for the permanents counted on the source controller's battlefield
 */
public record PermanentPowerAtMostControlledCountPredicate(PermanentPredicate countFilter)
        implements PermanentPredicate {
}
