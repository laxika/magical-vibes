package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Death trigger for auras: when the enchanted creature dies, return that creature card from
 * its owner's graveyard to the battlefield. Used by Abduction (owner's control) and
 * Unhallowed Pact ({@code underAuraControllersControl} — the Aura's controller gets it).
 *
 * <p>The {@code dyingCreatureCardId} is baked in at trigger time by
 * {@code DeathTriggerCollectorService} — it captures the dying creature's card ID so the
 * resolution logic can find it in the graveyard.</p>
 *
 * <p>Contrast {@link ReturnEnchantedCreatureToOwnerHandOnDeathEffect} (Demonic Vigor), which
 * returns the dying creature to its owner's hand instead of the battlefield.</p>
 *
 * @param dyingCreatureCardId          the card ID of the creature that just died; {@code null}
 *                                     in the card definition (baked in at trigger time)
 * @param underAuraControllersControl  {@code true} to put the creature onto the battlefield under
 *                                     the Aura controller's control, {@code false} for its owner's
 */
public record ReturnEnchantedCreatureToBattlefieldOnDeathEffect(
        UUID dyingCreatureCardId,
        boolean underAuraControllersControl
) implements CardEffect {

    /**
     * Card-definition constructor for the "under its owner's control" form — the dying creature's
     * card ID is not yet known.
     */
    public ReturnEnchantedCreatureToBattlefieldOnDeathEffect() {
        this(null, false);
    }

    /**
     * Card-definition constructor — the dying creature's card ID is not yet known.
     *
     * @param underAuraControllersControl see the record component
     */
    public ReturnEnchantedCreatureToBattlefieldOnDeathEffect(boolean underAuraControllersControl) {
        this(null, underAuraControllersControl);
    }
}
