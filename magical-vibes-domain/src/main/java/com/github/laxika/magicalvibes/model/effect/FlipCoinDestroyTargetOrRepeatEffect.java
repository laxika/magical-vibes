package com.github.laxika.magicalvibes.model.effect;

/**
 * Flips a coin to destroy the opponent's target creature on a win, or offers to pay to repeat the
 * flip on a loss. If the payment is declined, the controller's target creature is destroyed.
 * Both targets are retained by the activated ability while the effect repeats.
 */
public record FlipCoinDestroyTargetOrRepeatEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
