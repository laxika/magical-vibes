package com.github.laxika.magicalvibes.model.effect;

/**
 * Attaches a target Equipment to a target creature.
 * Used by cards like Brass Squire that can move equipment at instant speed.
 * Reads targetPermanentIds[0] as the equipment and [1] as the creature.
 */
public record AttachTargetEquipmentToTargetCreatureEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
