package com.github.laxika.magicalvibes.model.effect;

/**
 * "The next time a source of your choice would deal damage to you this turn, prevent that damage."
 * The source is chosen on resolution (any permanent, regardless of color). One-shot: only the next
 * damage event from that source is prevented, then the shield is consumed.
 *
 * <p>Like {@link PreventNextDamageFromChosenSourceMatchingEffect} but with no source restriction. When
 * {@code gainLife} is true the controller also gains life equal to the damage prevented this way
 * (Reverse Damage); when false there is no life gain (Pentagram of the Ages).
 *
 * @param gainLife whether the controller gains life equal to the prevented damage
 */
public record PreventNextDamageFromChosenSourceEffect(boolean gainLife) implements CardEffect {
}
