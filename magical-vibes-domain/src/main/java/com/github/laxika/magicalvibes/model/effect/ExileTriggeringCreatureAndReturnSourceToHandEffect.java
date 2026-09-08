package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Exiles the creature card that caused the trigger, then returns the trigger source to its owner's
 * hand. The trigger collector binds the dying card ID when it confirms that the creature is the
 * permanent linked to the source.
 *
 * @param dyingCardId the bound ID of the creature card that died
 */
public record ExileTriggeringCreatureAndReturnSourceToHandEffect(UUID dyingCardId) implements CardEffect {

    public ExileTriggeringCreatureAndReturnSourceToHandEffect() {
        this(null);
    }
}
