package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Static effect that prevents N damage from each source that would deal damage
 * to the attached (equipped/enchanted) creature.
 * (e.g. Shield of the Realm: "If a source would deal damage to equipped creature, prevent 2 of that damage.";
 * Shield of the Avatar prevents X where X is the number of creatures its controller controls.)
 *
 * @param amount the amount of damage to prevent per source, evaluated from the attachment's controller
 */
public record PreventXDamageFromEachSourceToAttachedCreatureEffect(DynamicAmount amount) implements CardEffect {
}
