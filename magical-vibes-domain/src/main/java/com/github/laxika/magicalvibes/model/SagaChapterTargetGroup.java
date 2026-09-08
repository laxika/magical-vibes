package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.TargetFilter;

/** A single target group used by a multi-target Saga chapter ability. */
public record SagaChapterTargetGroup(TargetFilter filter, int minTargets, int maxTargets) {
}
