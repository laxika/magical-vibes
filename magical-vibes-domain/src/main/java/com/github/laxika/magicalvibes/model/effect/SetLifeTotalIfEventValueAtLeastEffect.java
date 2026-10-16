package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/** Sets the controller's life total when the resolving entry's event value reaches a threshold. */
public record SetLifeTotalIfEventValueAtLeastEffect(int threshold, DynamicAmount lifeTotal) implements CardEffect {
}
