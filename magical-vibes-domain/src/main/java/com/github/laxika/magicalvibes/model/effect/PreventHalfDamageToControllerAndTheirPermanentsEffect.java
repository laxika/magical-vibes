package com.github.laxika.magicalvibes.model.effect;

/**
 * Static prevention effect: "If a source would deal damage to you or a permanent you control, prevent
 * half that damage, rounded up."
 *
 * <p>Recipient-scoped: half of the damage (rounded up) dealt to this permanent's controller or to any
 * permanent they control is prevented, so the amount actually dealt is {@code damage / 2} rounded down.
 * Like every prevention effect it does nothing while damage can't be prevented. Multiple instances
 * apply successively.
 *
 * <p>Applied in {@code DamagePreventionService.applyHalfDamagePrevention}, reached from both the player
 * choke point ({@code applyPlayerPreventionShield}) and the permanent choke point
 * ({@code applyCreaturePreventionShield}). Used by Gisela, Blade of Goldnight together with
 * {@link DoubleDamageToOpponentsAndTheirPermanentsEffect}.
 */
public record PreventHalfDamageToControllerAndTheirPermanentsEffect() implements CardEffect {
}
