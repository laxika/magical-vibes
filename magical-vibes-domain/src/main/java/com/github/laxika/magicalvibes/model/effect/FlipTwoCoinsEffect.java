package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper effect: "Flip two coins. If both heads, [bothHeads]. If both tails, [bothTails]."
 * Flips two coins at resolution time. If both come up heads, the bothHeads effect is dispatched.
 * If both come up tails, the bothTails effect is dispatched. On a split result, nothing happens.
 *
 * @param bothHeads the effect to execute when both coins are heads
 * @param bothTails the effect to execute when both coins are tails
 */
public record FlipTwoCoinsEffect(CardEffect bothHeads, CardEffect bothTails) implements CardEffect {
}
