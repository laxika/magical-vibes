package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * The controller flips several coins: "flip a coin until you lose a flip" ({@link #untilLose()}), "flip a coin that
 * many times or until you lose a flip" ({@link #upTo}), "flip that many coins" ({@link #flip}) or "flip N coins"
 * where only heads or tails matters ({@link #headsOrTails}).
 *
 * <p>Flips that stop at a loss are made one at a time, each its own flip; a counted flip that doesn't stop is one
 * flip of all the coins at once (Edgar, King of Figaro's ruling: an effect that flips several coins at once is one
 * set of flips). The step records {@link com.github.laxika.magicalvibes.model.StepKind#FLIP}: how many flips the
 * controller won (or how many coins came up heads, when no one wins them, CR 705.2), so "for each flip you won"
 * after it reads {@code AmountThisWay(FLIP)}.
 *
 * @param coins      how many coins to flip, evaluated once; {@code null} for no limit (until a flip is lost)
 * @param stopAtLoss whether the flipping stops at the first lost flip
 * @param contested  whether the controller wins or loses each flip (CR 705.2); {@code false} when only heads or tails
 *                   matters, so no one wins (but Edgar, King of Figaro makes them won flips)
 * @param perWin     resolved once for each won flip (each heads): right after it for flips that stop at a loss, in
 *                   the flips' order after all of them for a set flipped at once; or {@code null}
 * @param perLoss    resolved once for each lost flip of a set flipped at once, in the flips' order; or {@code null}
 * @param allWins    resolved after the flips when no flip was lost ("if you win all the flips"); or {@code null}
 */
public record FlipUntilLoseEffect(DynamicAmount coins, boolean stopAtLoss, boolean contested, CardEffect perWin,
                                  CardEffect perLoss, CardEffect allWins) implements CardEffect {

    public FlipUntilLoseEffect {
        if (coins == null && !stopAtLoss) {
            throw new IllegalArgumentException("Flips without a count stop at the first loss");
        }
        if (perLoss != null && stopAtLoss) {
            throw new IllegalArgumentException("Flips that stop at a loss have no per-loss effect");
        }
        if (!contested && (perLoss != null || allWins != null || stopAtLoss)) {
            throw new IllegalArgumentException("Heads-or-tails flips are neither won nor lost (CR 705.2)");
        }
    }

    /** "Flip a coin until you lose a flip." */
    public static FlipUntilLoseEffect untilLose() {
        return new FlipUntilLoseEffect(null, true, true, null, null, null);
    }

    /** "Flip a coin that many times or until you lose a flip, whichever comes first. If you win all the flips, …" */
    public static FlipUntilLoseEffect upTo(DynamicAmount coins, CardEffect allWins) {
        return new FlipUntilLoseEffect(coins, true, true, null, null, allWins);
    }

    /**
     * "Flip that many coins. For each flip you win, … For each flip you lose, … If you won them all, …"; any branch may
     * be {@code null}.
     */
    public static FlipUntilLoseEffect flip(DynamicAmount coins, CardEffect perWin, CardEffect perLoss,
                                           CardEffect allWins) {
        return new FlipUntilLoseEffect(coins, false, true, perWin, perLoss, allWins);
    }

    /** "Flip N coins. For each flip you win, …" */
    public static FlipUntilLoseEffect flip(int coins, CardEffect perWin) {
        return flip(new Fixed(coins), perWin, null, null);
    }

    /** "Flip N coins. … for each coin that comes up heads": no one wins or loses these flips (CR 705.2). */
    public static FlipUntilLoseEffect headsOrTails(int coins, CardEffect perHeads) {
        return new FlipUntilLoseEffect(new Fixed(coins), false, false, perHeads, null, null);
    }

    /** Delegates to whichever branch targets, so its target is chosen as the ability goes on the stack. */
    @Override
    public TargetSpec targetSpec() {
        for (CardEffect branch : new CardEffect[]{perWin, perLoss, allWins}) {
            if (branch != null && branch.targetSpec().declaredTarget() != null) {
                return branch.targetSpec();
            }
        }
        return TargetSpec.NONE;
    }
}
