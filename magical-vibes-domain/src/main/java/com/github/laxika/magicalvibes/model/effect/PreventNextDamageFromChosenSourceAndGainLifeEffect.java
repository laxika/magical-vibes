package com.github.laxika.magicalvibes.model.effect;

/**
 * "The next time a source of your choice would deal damage to you this turn, prevent that damage.
 * You gain life equal to the damage prevented this way." The source is chosen on resolution (any
 * permanent, regardless of color). One-shot: only the next damage event from that source is
 * prevented, then the shield is consumed. Reverse Damage.
 *
 * <p>Like {@link PreventNextDamageFromChosenColoredSourceEffect} but with no color restriction and
 * with the additional life gain equal to the damage prevented.
 */
public record PreventNextDamageFromChosenSourceAndGainLifeEffect() implements CardEffect {
}
