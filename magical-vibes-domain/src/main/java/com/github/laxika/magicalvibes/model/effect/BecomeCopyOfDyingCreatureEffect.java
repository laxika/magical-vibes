package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Triggered effect: the source permanent becomes a copy of a creature that just died,
 * except it keeps this triggered ability. Used by Cemetery Puca's
 * "you may pay {1}. If you do, this creature becomes a copy of that creature,
 * except it has this ability." death trigger.
 *
 * @param dyingCardId the card ID of the creature that died (null in the card definition,
 *                    filled in at trigger time from the dying creature's last-known information)
 */
public record BecomeCopyOfDyingCreatureEffect(UUID dyingCardId) implements CardEffect {

    public BecomeCopyOfDyingCreatureEffect() {
        this(null);
    }
}
