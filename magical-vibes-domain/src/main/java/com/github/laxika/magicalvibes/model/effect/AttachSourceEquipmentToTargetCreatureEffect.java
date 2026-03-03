package com.github.laxika.magicalvibes.model.effect;

/**
 * Attaches the source equipment to a target creature.
 * Used by equipment with "When this Equipment enters, attach it to target creature you control."
 * Reads targetPermanentId as the creature to attach to, and sourcePermanentId as the equipment.
 */
public record AttachSourceEquipmentToTargetCreatureEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
