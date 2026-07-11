package com.github.laxika.magicalvibes.model.effect;

/**
 * "Whenever a [subtype] creature enters, you may attach this Equipment to it."
 *
 * <p>Trigger-materialising marker for {@code ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD}. Unlike
 * {@link AttachSourceEquipmentToTargetCreatureEffect} this does not target — "it" is the creature
 * that just entered. The enter collector resolves the entering permanent and queues a
 * {@code MayEffect(AttachSourceEquipmentToTargetCreatureEffect)} with {@code targetId} set to the
 * entering creature and {@code sourcePermanentId} set to this Equipment, so the equipment may be
 * attached to a creature under any player's control (CR 301.5c). Used by Cloak and Dagger (Rogue).
 */
public record AttachSourceEquipmentToEnteringCreatureEffect() implements CardEffect {
}
