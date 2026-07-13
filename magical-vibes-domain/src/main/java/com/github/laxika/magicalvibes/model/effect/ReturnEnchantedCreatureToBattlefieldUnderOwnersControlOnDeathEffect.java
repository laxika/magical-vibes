package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Death trigger for auras: when the enchanted creature dies, return that creature card from
 * its owner's graveyard to the battlefield under its owner's control. Used by Abduction.
 *
 * <p>The {@code dyingCreatureCardId} is baked in at trigger time by
 * {@code DeathTriggerCollectorService} — it captures the dying creature's card ID so the
 * resolution logic can find it in the graveyard.</p>
 *
 * <p>Contrast {@link ReturnEnchantedCreatureToOwnerHandOnDeathEffect} (Demonic Vigor), which
 * returns the dying creature to its owner's hand instead of the battlefield.</p>
 *
 * @param dyingCreatureCardId the card ID of the creature that just died; {@code null}
 *                            in the card definition (baked in at trigger time)
 */
public record ReturnEnchantedCreatureToBattlefieldUnderOwnersControlOnDeathEffect(
        UUID dyingCreatureCardId
) implements CardEffect {

    /**
     * Card-definition constructor — the dying creature's card ID is not yet known.
     */
    public ReturnEnchantedCreatureToBattlefieldUnderOwnersControlOnDeathEffect() {
        this((UUID) null);
    }
}
