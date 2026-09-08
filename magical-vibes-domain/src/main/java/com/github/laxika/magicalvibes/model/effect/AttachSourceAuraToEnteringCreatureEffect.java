package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger-materialising marker for attaching the source Aura to the creature that just entered.
 * The enter collector either queues a may-attach choice or a mandatory attachment, depending on
 * {@link #optional()}.
 */
public record AttachSourceAuraToEnteringCreatureEffect(boolean optional) implements CardEffect {

    public AttachSourceAuraToEnteringCreatureEffect() {
        this(true);
    }
}
