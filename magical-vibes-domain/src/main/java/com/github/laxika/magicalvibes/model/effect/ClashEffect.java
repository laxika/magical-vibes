package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * The clash-source effect (CR 701.30): resolves the {@code beforeClash} effects in order,
 * then the controller chooses an opponent and clashes with them (CR 701.30b) — both reveal the top card of
 * their library, the controller wins if their revealed card's mana value is strictly greater.
 * On a win the {@code onWin} effect (if any) is dispatched, and if {@code repeatWhileWinning}
 * the whole sequence (beforeClash, then clash) repeats until the controller loses a clash.
 *
 * <p>Covers every clash-source template:
 * <ul>
 *   <li>{@code new ClashEffect(wrapped)} — "Clash with an opponent. If you win, [wrapped]."
 *       (or a bare clash with {@code wrapped = null}, e.g. Whirlpool Whelm)</li>
 *   <li>{@code new ClashEffect(body, null, true)} — "[body], then clash with an opponent.
 *       If you win, repeat this process." (e.g. Hoarder's Greed)</li>
 * </ul>
 *
 * <p>This effect <em>initiates</em> a clash from a spell/ability resolution. A "whenever you clash" ability
 * ({@code Triggers.youClash()}) reads the clash's outcome as its event value: 1 if its controller won, else 0.
 *
 * @param beforeClash        effects executed at the start of each iteration, before the clash
 * @param onWin              the effect to execute when the controller wins the clash (may be
 *                           {@code null} for a clash with no win reward)
 * @param repeatWhileWinning whether the whole sequence repeats while the controller keeps winning
 * @param beforeClashResolved true once the {@code beforeClash} effects have been queued ahead of this
 *                           record, so that only the clash itself remains to be performed
 * @param opponent           the opponent clashed with, or {@code null} until the controller has chosen one (the
 *                           one opponent, unasked)
 */
public record ClashEffect(List<CardEffect> beforeClash, CardEffect onWin, boolean repeatWhileWinning,
                          boolean beforeClashResolved, PlayerSubject opponent)
        implements CardEffect {

    public ClashEffect(List<CardEffect> beforeClash, CardEffect onWin, boolean repeatWhileWinning,
                       boolean beforeClashResolved) {
        this(beforeClash, onWin, repeatWhileWinning, beforeClashResolved, null);
    }

    /** This clash with the given opponent. */
    public ClashEffect against(PlayerSubject chosenOpponent) {
        return new ClashEffect(beforeClash, onWin, repeatWhileWinning, beforeClashResolved, chosenOpponent);
    }

    /** "Clash with an opponent. If you win, [onWin]." — the plain clash-then-reward template. */
    public ClashEffect(CardEffect onWin) {
        this(List.of(), onWin, false, false);
    }

    public ClashEffect(List<CardEffect> beforeClash, CardEffect onWin, boolean repeatWhileWinning) {
        this(beforeClash, onWin, repeatWhileWinning, false);
    }

    @Override
    public TargetSpec targetSpec() {
        boolean perm = (onWin != null && onWin.targetSpec().admits(TargetPredicate.Kind.PERMANENT))
                || beforeClash.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
        boolean player = (onWin != null && onWin.targetSpec().admits(TargetPredicate.Kind.PLAYER))
                || beforeClash.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
        TargetPredicate declaredTarget = perm
                ? (player ? TargetPredicates.playerOrPermanent() : TargetPredicates.permanent())
                : (player ? TargetPredicates.player() : null);
        return new TargetSpec(declaredTarget, false, null, false, 1);
    }
}
