package com.github.laxika.magicalvibes.model.amount;

/**
 * The resolved target permanent's effective power at resolution time. Negative values are
 * retained only when {@code allowNegative} is true, for effects that double power. Evaluates
 * to 0 when there is no legal target, matching the fizzle behaviour of the handlers it replaces.
 * Reads the target from the stack entry the same way {@code ConditionContext.targetId} does.
 */
public record TargetPower(boolean allowNegative) implements DynamicAmount {

    public TargetPower() {
        this(false);
    }
}
