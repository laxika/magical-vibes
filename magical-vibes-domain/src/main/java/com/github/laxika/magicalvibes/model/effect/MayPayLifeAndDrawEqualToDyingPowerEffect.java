package com.github.laxika.magicalvibes.model.effect;

/**
 * ON_EQUIPPED_CREATURE_DIES marker for an optional payment of life equal to the dying creature's
 * last-known effective power, followed by drawing that many cards.
 *
 * <p>The equipped-creature death collector snapshots the power and materializes this marker as a
 * {@link MayPayLifeEffect} containing a fixed-size {@link DrawCardEffect}.
 */
public record MayPayLifeAndDrawEqualToDyingPowerEffect() implements CardEffect {
}
