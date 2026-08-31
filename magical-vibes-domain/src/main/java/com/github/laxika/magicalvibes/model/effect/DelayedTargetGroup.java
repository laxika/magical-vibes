package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.TargetFilter;

/** A target group that is created when a delayed trigger is put onto the stack. */
public record DelayedTargetGroup(TargetFilter filter, int minTargets, int maxTargets) {
}
