package com.github.laxika.magicalvibes.model.amount;

/**
 * Evaluates to {@code amount} when all announced targets are creatures controlled by the
 * activating player, and to {@code otherwise} in every other activation context.
 */
public record FixedIfAllTargetsControlledByController(int amount, int otherwise)
        implements DynamicAmount {
}
