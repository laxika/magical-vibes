package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect for Sokrates, Athenian Teacher: prevents combat damage from the
 * permanent carrying this effect to a player, then makes its controller and that player each draw
 * half the prevented damage, rounded down.
 */
public record PreventCombatDamageToPlayerAndDrawHalfEffect() implements CardEffect {
}
