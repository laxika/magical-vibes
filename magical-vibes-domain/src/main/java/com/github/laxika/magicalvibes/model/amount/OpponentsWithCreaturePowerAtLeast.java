package com.github.laxika.magicalvibes.model.amount;

/** The number of opponents who control a creature with at least the given power. */
public record OpponentsWithCreaturePowerAtLeast(int minPower) implements DynamicAmount {
}
