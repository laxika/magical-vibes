package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Repeat the following process {@code N} times. Each opponent loses {@code lifeLoss} life unless
 * that player sacrifices a matching permanent of their choice or discards a card." Each opponent, in
 * APNAP order, chooses one of the three outcomes independently each iteration; a player who can
 * neither sacrifice a matching permanent nor discard simply loses life (they may always choose to
 * lose life). The default matching permanent is any nonland permanent. Resolved by
 * {@code TormentOfHailfireEffectHandler}.
 *
 * <p>When {@code fixedIterations} is {@code null}, {@code N} is the resolving stack entry's
 * {@code xValue} (Torment of Hailfire). When non-null, that fixed count is used instead
 * ({@link #once(int)} for a single pass — Nicol Bolas, the Deceiver +3).
 *
 * @param lifeLoss         life each opponent loses when they don't sacrifice or discard
 * @param fixedIterations  fixed pass count, or {@code null} to use the stack entry's {@code xValue}
 * @param sacrificePredicate permanents eligible for the sacrifice option, or {@code null} for
 *                           nonland permanents
 */
public record TormentOfHailfireEffect(int lifeLoss, Integer fixedIterations,
                                      PermanentPredicate sacrificePredicate) implements CardEffect {

    /** Uses the stack entry's {@code xValue} as the iteration count (Torment of Hailfire). */
    public TormentOfHailfireEffect(int lifeLoss) {
        this(lifeLoss, null, null);
    }

    /** Uses a custom permanent filter for the sacrifice option. */
    public TormentOfHailfireEffect(int lifeLoss, PermanentPredicate sacrificePredicate) {
        this(lifeLoss, null, sacrificePredicate);
    }

    /** One pass over each opponent (Nicol Bolas, the Deceiver +3). */
    public static TormentOfHailfireEffect once(int lifeLoss) {
        return new TormentOfHailfireEffect(lifeLoss, 1, null);
    }

    /** One pass over each opponent with a custom sacrifice filter. */
    public static TormentOfHailfireEffect once(int lifeLoss, PermanentPredicate sacrificePredicate) {
        return new TormentOfHailfireEffect(lifeLoss, 1, sacrificePredicate);
    }
}
