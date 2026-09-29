package com.github.laxika.magicalvibes.model.amount;

/** The sum of the effective power of the creatures chosen in a target group. */
public record TotalPowerOfTargetGroup(int groupIndex) implements DynamicAmount {
}
