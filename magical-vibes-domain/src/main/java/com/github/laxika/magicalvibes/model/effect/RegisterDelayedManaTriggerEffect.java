package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * When resolved (typically from an accepted MayEffect during opening hand reveal),
 * registers a delayed trigger that adds mana at the beginning of the revealing
 * player's first precombat main phase of the game.
 * <p>
 * Used by Chancellor of the Tangle's opening hand ability.
 *
 * @param color  the color of mana to add
 * @param amount the amount of mana to add
 */
public record RegisterDelayedManaTriggerEffect(ManaColor color, int amount) implements CardEffect {
}
