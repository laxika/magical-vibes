package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved (typically from an accepted MayEffect during opening hand reveal),
 * registers a delayed trigger that counters each opponent's first spell of the game
 * unless they pay the specified generic mana amount.
 * <p>
 * Used by Chancellor of the Annex's opening hand ability.
 *
 * @param genericManaAmount the generic mana the opponent must pay to avoid the counter
 */
public record RegisterDelayedCounterTriggerEffect(int genericManaAmount) implements CardEffect {
}
