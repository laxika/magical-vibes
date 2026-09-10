package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player discards 1/divisor of the cards in their hand, rounded down, chosen by that player.
 */
public record EachPlayerDiscardsFractionOfHandRoundedDownEffect(int divisor) implements CardEffect {
}
