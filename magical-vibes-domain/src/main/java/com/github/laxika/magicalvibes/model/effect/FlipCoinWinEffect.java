package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper effect: "Flip a coin. If you win the flip, [wrapped effect].
 * If you lose the flip, [lost effect]."
 * Flips a coin at resolution time; if the controller wins, the wrapped
 * effect is dispatched. If the controller loses, the lost effect is
 * dispatched (or nothing happens when {@code lost} is null).
 *
 * @param wrapped the effect to execute on a coin flip win
 * @param lost    the effect to execute on a coin flip loss (may be null)
 */
public record FlipCoinWinEffect(CardEffect wrapped, CardEffect lost) implements CardEffect {

    /** Coin flip with only a win effect (nothing happens on a loss). */
    public FlipCoinWinEffect(CardEffect wrapped) {
        this(wrapped, null);
    }

    /**
     * Delegates to whichever branch targets, so a targeted flip reward works on a spell or
     * activated ability (the target is chosen when the ability goes on the stack, before the flip).
     * Goblin Lyre's win branch targets an opponent or planeswalker; the loss branch does not.
     */
    @Override
    public TargetSpec targetSpec() {
        TargetSpec wonSpec = wrapped == null ? TargetSpec.NONE : wrapped.targetSpec();
        if (wonSpec.category() != TargetCategory.NONE) {
            return wonSpec;
        }
        return lost == null ? TargetSpec.NONE : lost.targetSpec();
    }
}
