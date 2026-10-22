package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;
import com.github.laxika.magicalvibes.model.Permanent;

/**
 * Triggered effect: the source permanent becomes a copy of a creature that just entered the
 * battlefield, except it keeps this triggered ability. Used by Unstable Shapeshifter's
 * "Whenever another creature enters, this creature becomes a copy of that creature,
 * except it has this ability."
 *
 * @param enteringPermanentId the battlefield id of the creature that entered (null in the card
 *                            definition, filled in at trigger time)
 * @param enteringPermanent   the entering creature's permanent object, used as last-known information
 *                            when it is no longer on the battlefield at resolution
 */
public record BecomeCopyOfEnteringCreatureEffect(UUID enteringPermanentId, Permanent enteringPermanent) implements CardEffect {

    public BecomeCopyOfEnteringCreatureEffect(UUID enteringPermanentId) {
        this(enteringPermanentId, null);
    }

    public BecomeCopyOfEnteringCreatureEffect() {
        this(null);
    }
}
