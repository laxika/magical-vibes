package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.TargetFilter;

/**
 * A single target group used by a multi-target Saga chapter ability.
 *
 * @param maxTotalManaValue the maximum combined mana value of the chosen targets;
 *                          {@link Integer#MAX_VALUE} means uncapped
 */
public record SagaChapterTargetGroup(TargetFilter filter, int minTargets, int maxTargets,
                                     int maxTotalManaValue) {

    public SagaChapterTargetGroup(TargetFilter filter, int minTargets, int maxTargets) {
        this(filter, minTargets, maxTargets, Integer.MAX_VALUE);
    }
}
