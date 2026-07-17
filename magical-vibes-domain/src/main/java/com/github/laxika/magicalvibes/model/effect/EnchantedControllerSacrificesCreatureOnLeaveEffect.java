package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Leaves-the-battlefield trigger for Funeral March: "When enchanted creature leaves the battlefield,
 * its controller sacrifices a creature of their choice."
 *
 * <p>Placed in {@code EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD}. The enchanted creature's
 * controller (who lost the creature) is baked in at trigger time by {@code DeathTriggerCollectorService}
 * — this is <em>not</em> the Aura's controller, which matters when the Aura enchants an opponent's
 * creature. The baked player then sacrifices a creature of their choice at resolution.
 *
 * @param enchantedControllerId the player who controlled the leaving creature; {@code null} in the
 *                              card definition (baked in at trigger time)
 */
public record EnchantedControllerSacrificesCreatureOnLeaveEffect(
        UUID enchantedControllerId
) implements CardEffect {

    /**
     * Card-definition constructor — the enchanted creature's controller is not yet known.
     */
    public EnchantedControllerSacrificesCreatureOnLeaveEffect() {
        this((UUID) null);
    }
}
