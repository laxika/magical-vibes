package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose a source you control and flip a coin. If you win the flip, the next time that source would
 * deal damage this turn, it deals double that damage instead. If you lose the flip, the next time it
 * would deal damage this turn, prevent that damage." (Desperate Gambit).
 *
 * <p>The source is chosen on resolution among the permanents its controller controls; the coin is
 * flipped after the choice. Both outcomes install the same one-shot
 * {@code SourceNextDamageToAnyTargetShield} — a x2 multiplier on a win, a x0 (prevention) multiplier
 * on a loss — so the next damage event from that source, combat or noncombat, to any recipient,
 * consumes it.
 */
public record FlipCoinDoubleOrPreventNextDamageFromChosenSourceEffect() implements CardEffect {
}
