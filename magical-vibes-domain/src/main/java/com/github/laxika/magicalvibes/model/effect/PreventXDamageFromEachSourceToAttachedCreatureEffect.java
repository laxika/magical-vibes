package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that prevents N damage from each source that would deal damage
 * to the attached (equipped/enchanted) creature.
 * (e.g. Shield of the Realm: "If a source would deal damage to equipped creature, prevent 2 of that damage.")
 *
 * @param amount the amount of damage to prevent per source
 */
public record PreventXDamageFromEachSourceToAttachedCreatureEffect(int amount) implements CardEffect {
}
