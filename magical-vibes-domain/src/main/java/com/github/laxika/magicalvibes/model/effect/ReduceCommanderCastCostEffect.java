package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/** Reduces the generic cost of the source controller's designated commander by a dynamic amount. */
public record ReduceCommanderCastCostEffect(DynamicAmount amount) implements CardEffect {
}
