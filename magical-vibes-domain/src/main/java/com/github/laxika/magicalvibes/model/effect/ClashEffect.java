package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * The clash-source effect (MTG rule 701.29): resolves the {@code beforeClash} effects in order,
 * then the controller clashes against their (2-player) opponent — both reveal the top card of
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
 * <p>This is the clash-<em>source</em> counterpart to {@link IfWonClashEffect}: this effect
 * <em>initiates</em> a clash from a spell/ability resolution, whereas {@code IfWonClashEffect}
 * is a "whenever you clash" trigger clause consumed by {@code TriggerCollectionService}.
 *
 * @param beforeClash        effects executed at the start of each iteration, before the clash
 * @param onWin              the effect to execute when the controller wins the clash (may be
 *                           {@code null} for a clash with no win reward)
 * @param repeatWhileWinning whether the whole sequence repeats while the controller keeps winning
 */
public record ClashEffect(List<CardEffect> beforeClash, CardEffect onWin, boolean repeatWhileWinning)
        implements CardEffect {

    /** "Clash with an opponent. If you win, [onWin]." — the plain clash-then-reward template. */
    public ClashEffect(CardEffect onWin) {
        this(List.of(), onWin, false);
    }

    @Override
    public TargetSpec targetSpec() {
        boolean perm = (onWin != null && onWin.targetSpec().category().includesPermanents())
                || beforeClash.stream().anyMatch(e -> e.targetSpec().category().includesPermanents());
        boolean player = (onWin != null && onWin.targetSpec().category().includesPlayers())
                || beforeClash.stream().anyMatch(e -> e.targetSpec().category().includesPlayers());
        TargetCategory category = perm
                ? (player ? TargetCategory.PLAYER_OR_PERMANENT : TargetCategory.PERMANENT)
                : (player ? TargetCategory.PLAYER : TargetCategory.NONE);
        return new TargetSpec(category, false, null, false, 1);
    }
}
