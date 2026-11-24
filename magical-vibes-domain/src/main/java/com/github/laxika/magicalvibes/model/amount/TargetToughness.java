package com.github.laxika.magicalvibes.model.amount;

/**
 * The resolved target permanent's effective toughness at resolution time. Evaluates to 0 when there is no legal
 * target, matching the fizzle behaviour of the handlers it replaces. Reads the target from the stack entry the same way
 * {@code ConditionContext.targetId} does. A negative toughness counts as 0 unless {@code allowNegative}: an effect that
 * sets a creature's power or toughness to it keeps the negative value (CR 107.1b).
 *
 * @param allowNegative whether a negative toughness is kept rather than counted as 0
 */
public record TargetToughness(boolean allowNegative) implements DynamicAmount {

    public TargetToughness() {
        this(false);
    }
}
