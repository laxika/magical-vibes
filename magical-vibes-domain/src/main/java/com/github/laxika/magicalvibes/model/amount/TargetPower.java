package com.github.laxika.magicalvibes.model.amount;

/**
 * The resolved target permanent's effective power at resolution time (never negative). Evaluates
 * to 0 when there is no legal target, matching the fizzle behaviour of the handlers it replaces.
 * Reads the target from the stack entry the same way {@code ConditionContext.targetId} does.
 */
public record TargetPower() implements DynamicAmount {
}
