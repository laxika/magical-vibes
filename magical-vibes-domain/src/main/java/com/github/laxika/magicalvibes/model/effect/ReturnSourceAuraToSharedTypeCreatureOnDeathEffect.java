package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Death trigger for auras: when the enchanted creature dies, you may return this aura from its
 * owner's graveyard to the battlefield attached to a creature that shares a creature type with the
 * creature that died. Used by Reins of the Vinesteed.
 *
 * <p>The {@code dyingCreatureCardId} is baked in at trigger time by
 * {@code DeathTriggerCollectorService} — it captures the dying creature so the resolution logic can
 * read its creature types (last-known information from the graveyard) to decide which creatures on
 * the battlefield are valid attachment targets. The card-definition instance leaves it {@code null};
 * the collector wraps the baked effect in a {@link MayEffect} for the optional "you may return".</p>
 *
 * @param dyingCreatureCardId the card ID of the creature that just died; {@code null} in the card
 *                            definition (baked in at trigger time)
 */
public record ReturnSourceAuraToSharedTypeCreatureOnDeathEffect(
        UUID dyingCreatureCardId
) implements CardEffect {

    /**
     * Card-definition constructor — the dying creature's ID is not yet known.
     */
    public ReturnSourceAuraToSharedTypeCreatureOnDeathEffect() {
        this((UUID) null);
    }
}
