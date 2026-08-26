package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that prevents a fixed amount of each damage event dealt by a spell to a permanent
 * or player.
 */
public record PreventFixedDamageFromSpellsEffect(int amount) implements CardEffect {
}
