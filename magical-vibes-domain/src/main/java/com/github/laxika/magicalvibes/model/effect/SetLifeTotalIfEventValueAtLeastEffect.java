package com.github.laxika.magicalvibes.model.effect;

/** Sets the controller's life total when the resolving entry's event value reaches a threshold. */
public record SetLifeTotalIfEventValueAtLeastEffect(int threshold, int lifeTotal) implements CardEffect {
}
