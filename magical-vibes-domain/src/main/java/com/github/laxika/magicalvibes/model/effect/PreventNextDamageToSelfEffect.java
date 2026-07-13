package com.github.laxika.magicalvibes.model.effect;

/**
 * "Prevent the next {@code amount} damage that would be dealt to this creature this turn." The
 * protected creature is this ability's source permanent (no target). Applies to the next
 * {@code amount} damage from any source (combat or noncombat) via the permanent's damage
 * prevention shield, then the shield is consumed. Used by Ethereal Champion.
 */
public record PreventNextDamageToSelfEffect(int amount) implements CardEffect {
}
