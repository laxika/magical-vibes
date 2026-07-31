package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys the creature that the Equipment enchanted by the source Aura is attached to. Does nothing
 * when the Aura is unattached, when the enchanted Equipment is attached to nothing, or when the thing
 * it is attached to is no longer a creature. Placed in the {@code UPKEEP_TRIGGERED} slot; resolved by
 * {@code DestroyCreatureAttachedToEnchantedEquipmentEffectHandler}. Used by Artificer's Hex
 * ("At the beginning of your upkeep, if enchanted Equipment is attached to a creature, destroy that creature").
 */
public record DestroyCreatureAttachedToEnchantedEquipmentEffect() implements CardEffect {
}
