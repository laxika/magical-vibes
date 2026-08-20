package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller wins if the current stack entry's event value has reached the threshold.
 */
public record WinGameIfEventValueAtLeastEffect(int threshold) implements CardEffect {
}
