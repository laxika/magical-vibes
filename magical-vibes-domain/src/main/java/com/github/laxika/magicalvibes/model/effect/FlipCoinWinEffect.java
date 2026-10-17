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
 * @param countsAsWinOrLoss whether the flip is a win/loss flip (CR 705.2) that fires "whenever you win or lose a
 *                          coin flip" triggers; {@code false} for heads-or-tails flips such as Odds
 */
public record FlipCoinWinEffect(CardEffect wrapped, CardEffect lost,
                                boolean triggeringSpellController, UUID flippingPlayerId,
                                boolean countsAsWinOrLoss) implements CardEffect {

    public FlipCoinWinEffect(CardEffect wrapped, CardEffect lost) {
        this(wrapped, lost, false, null, true);
    }

    public FlipCoinWinEffect(CardEffect wrapped, CardEffect lost,
                             boolean triggeringSpellController, UUID flippingPlayerId) {
        this(wrapped, lost, triggeringSpellController, flippingPlayerId, true);
    }

    public static FlipCoinWinEffect forTriggeringSpellController(CardEffect wrapped, CardEffect lost) {
        return new FlipCoinWinEffect(wrapped, lost, true, null, true);
    }

    /**
     * Flip a coin where only heads or tails matters: heads dispatches {@code heads}, tails dispatches
     * {@code tails}, and neither result is a win or a loss of the flip (CR 705.2).
     */
    public static FlipCoinWinEffect headsOrTails(CardEffect heads, CardEffect tails) {
        return new FlipCoinWinEffect(heads, tails, false, null, false);
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
