package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Leaves-the-battlefield trigger for reanimation auras (e.g. Animate Dead): "When this Aura leaves
 * the battlefield, that creature's controller sacrifices it."
 *
 * <p>Placed in {@code EffectSlot.ON_SELF_LEAVES_BATTLEFIELD}. The enchanted creature's permanent ID
 * is baked in at trigger time by {@code DeathTriggerCollectorService} — it captures the aura's
 * {@code attachedTo} before the aura is gone, so the resolution logic can sacrifice the right
 * permanent even though the aura is no longer on the battlefield.</p>
 *
 * @param enchantedPermanentId the permanent ID of the creature the aura was attached to; {@code null}
 *                             in the card definition (baked in at trigger time)
 */
public record SacrificeEnchantedCreatureOnLeaveEffect(
        UUID enchantedPermanentId
) implements CardEffect {

    /**
     * Card-definition constructor — the enchanted creature's permanent ID is not yet known.
     */
    public SacrificeEnchantedCreatureOnLeaveEffect() {
        this((UUID) null);
    }
}
