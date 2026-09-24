package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Wrapper effect: "Flip a coin. If you win the flip, [wrapped effect].
 * If you lose the flip, [lost effect]."
 * Flips a coin at resolution time; if the controller wins, the wrapped
 * effect is dispatched. If the controller loses, the lost effect is
 * dispatched (or nothing happens when {@code lost} is null).
 *
 * @param wrapped the effect to execute on a coin flip win
 * @param lost    the effect to execute on a coin flip loss (may be null)
 * @param triggeringSpellController whether the spell-cast collector binds the caster as the flipping player
 * @param flippingPlayerId the player bound when the trigger is collected, or null for the ability's controller
 */
public record FlipCoinWinEffect(CardEffect wrapped, CardEffect lost,
                                boolean triggeringSpellController, UUID flippingPlayerId) implements CardEffect {

    public FlipCoinWinEffect(CardEffect wrapped, CardEffect lost) {
        this(wrapped, lost, false, null);
    }

    public static FlipCoinWinEffect forTriggeringSpellController(CardEffect wrapped, CardEffect lost) {
        return new FlipCoinWinEffect(wrapped, lost, true, null);
    }

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
        if (wonSpec.declaredTarget() != null) {
            return wonSpec;
        }
        return lost == null ? TargetSpec.NONE : lost.targetSpec();
    }
}
