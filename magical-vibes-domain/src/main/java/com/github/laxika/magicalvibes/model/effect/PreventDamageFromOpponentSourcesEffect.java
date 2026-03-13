package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that prevents N damage from each opponent-controlled source
 * that would deal damage to this permanent's controller.
 * (e.g. Guardian Seraph: "If a source an opponent controls would deal damage to you, prevent 1 of that damage.")
 *
 * @param amount the amount of damage to prevent per source
 */
public record PreventDamageFromOpponentSourcesEffect(int amount) implements CardEffect {
}
