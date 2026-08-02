package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Triggered effect: the source permanent becomes a copy of a creature that just entered the
 * battlefield, except it keeps this triggered ability. Used by Unstable Shapeshifter's
 * "Whenever another creature enters, this creature becomes a copy of that creature,
 * except it has this ability."
 *
 * @param enteringPermanentId the battlefield id of the creature that entered (null in the card
 *                            definition, filled in at trigger time)
 */
public record BecomeCopyOfEnteringCreatureEffect(UUID enteringPermanentId) implements CardEffect {

    public BecomeCopyOfEnteringCreatureEffect() {
        this(null);
    }
}
