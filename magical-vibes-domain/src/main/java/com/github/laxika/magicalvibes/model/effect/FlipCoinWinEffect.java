package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper effect: "Flip a coin. If you win the flip, [wrapped effect]."
 * Flips a coin at resolution time; if the controller wins, the wrapped
 * effect is dispatched. If the controller loses, nothing happens.
 *
 * @param wrapped the effect to execute on a coin flip win
 */
public record FlipCoinWinEffect(CardEffect wrapped) implements CardEffect {
}
