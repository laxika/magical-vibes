package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Death trigger for auras: when the enchanted creature dies, return that creature card from
 * its owner's graveyard to its owner's hand. Used by Demonic Vigor.
 *
 * <p>The {@code dyingCreatureCardId} is baked in at trigger time by
 * {@code DeathTriggerService.checkEnchantedPermanentDeathTriggers()} — it captures the dying
 * creature's card ID so the resolution logic can find it in the graveyard.</p>
 *
 * @param dyingCreatureCardId the card ID of the creature that just died; {@code null}
 *                            in the card definition (baked in at trigger time)
 */
public record ReturnEnchantedCreatureToOwnerHandOnDeathEffect(
        UUID dyingCreatureCardId
) implements CardEffect {

    /**
     * Card-definition constructor — the dying creature's card ID is not yet known.
     */
    public ReturnEnchantedCreatureToOwnerHandOnDeathEffect() {
        this((UUID) null);
    }
}
