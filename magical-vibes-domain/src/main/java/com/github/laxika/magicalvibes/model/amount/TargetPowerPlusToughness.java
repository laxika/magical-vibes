package com.github.laxika.magicalvibes.model.amount;

/**
 * The resolved target permanent's effective power plus toughness at resolution time. Unlike
 * {@link TargetPower} and {@link TargetToughness}, negative values are preserved because some
 * effects explicitly add the two characteristics together (Phthisis).
 */
public record TargetPowerPlusToughness() implements DynamicAmount {
}
