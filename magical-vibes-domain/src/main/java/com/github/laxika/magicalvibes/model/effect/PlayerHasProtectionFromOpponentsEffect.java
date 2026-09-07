package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: the source permanent's controller has protection from each of their opponents.
 * The battlefield query layer evaluates the controller relationship against the source of a
 * spell, ability, damage, or Aura as needed.
 */
public record PlayerHasProtectionFromOpponentsEffect() implements CardEffect {
}
