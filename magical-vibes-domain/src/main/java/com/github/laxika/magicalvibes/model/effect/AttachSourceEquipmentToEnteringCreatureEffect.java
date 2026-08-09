package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger-materialising marker for an Equipment attachment to the creature that just entered.
 * The enter collector either queues a may-attach choice or a mandatory non-targeting attachment,
 * depending on {@link #optional()}.
 */
public record AttachSourceEquipmentToEnteringCreatureEffect(boolean optional) implements CardEffect {

    public AttachSourceEquipmentToEnteringCreatureEffect() {
        this(true);
    }
}
