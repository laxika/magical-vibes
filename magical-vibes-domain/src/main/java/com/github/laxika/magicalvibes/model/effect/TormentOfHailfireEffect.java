package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * "Repeat the following process {@code N} times. Each opponent loses {@code lifeLoss} life unless
 * that player sacrifices a nonland permanent of their choice or discards a card." Each opponent, in
 * APNAP order, chooses one of the three outcomes independently each iteration; a player who can
 * neither sacrifice a nonland permanent nor discard simply loses life (they may always choose to
 * lose life). Resolved by {@code TormentOfHailfireEffectHandler}.
 *
 * <p>When {@code dynamicIterations} is non-null, it supplies {@code N} at resolution time.
 * Otherwise, when {@code fixedIterations} is {@code null}, {@code N} is the resolving stack
 * entry's {@code xValue} (Torment of Hailfire). When non-null, that fixed count is used instead
 * ({@link #once(int)} for a single pass — Nicol Bolas, the Deceiver +3).
 *
 * @param lifeLoss         life each opponent loses when they don't sacrifice or discard
 * @param fixedIterations  fixed pass count, or {@code null} to use the stack entry's {@code xValue}
 * @param dynamicIterations pass count evaluated from the resolving source, or {@code null}
 */
public record TormentOfHailfireEffect(int lifeLoss, Integer fixedIterations,
                                      DynamicAmount dynamicIterations) implements CardEffect {

    /** Uses the stack entry's {@code xValue} as the iteration count (Torment of Hailfire). */
    public TormentOfHailfireEffect(int lifeLoss) {
        this(lifeLoss, null, null);
    }

    /** Uses a fixed pass count, retaining the two-argument constructor's original API. */
    public TormentOfHailfireEffect(int lifeLoss, Integer fixedIterations) {
        this(lifeLoss, fixedIterations, null);
    }

    /** One pass over each opponent (Nicol Bolas, the Deceiver +3). */
    public static TormentOfHailfireEffect once(int lifeLoss) {
        return new TormentOfHailfireEffect(lifeLoss, 1, null);
    }
}
