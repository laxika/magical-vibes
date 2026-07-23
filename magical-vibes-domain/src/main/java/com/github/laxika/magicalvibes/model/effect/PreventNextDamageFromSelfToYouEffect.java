package com.github.laxika.magicalvibes.model.effect;

/**
 * "{cost}: The next time this creature would deal damage to you this turn, prevent that damage."
 * Installs a one-shot {@code PlayerSourceNextDamageShield} keyed to the ability's source permanent
 * and the activating player (stack {@code controllerId}). Same consumption path as Circle of
 * Protection ({@code DamagePreventionService.applyPlayerNextSourceDamageShield}). Used by
 * Mercenaries; typically paired with {@code ActivatedAbility.withActivatableByAnyPlayer()}.
 */
public record PreventNextDamageFromSelfToYouEffect() implements CardEffect {
}
