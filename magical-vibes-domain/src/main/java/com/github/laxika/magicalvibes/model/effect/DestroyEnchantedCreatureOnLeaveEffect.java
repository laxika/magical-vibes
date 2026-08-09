package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Leaves-the-battlefield trigger for an Aura that destroys the creature it enchanted.
 *
 * <p>The enchanted creature's permanent ID is captured when the Aura leaves, because the Aura is
 * no longer on the battlefield when the trigger resolves.</p>
 *
 * @param enchantedPermanentId the creature the Aura was attached to, or {@code null} in the card
 *                             definition before trigger collection
 * @param cannotBeRegenerated  whether the destruction ignores regeneration shields
 */
public record DestroyEnchantedCreatureOnLeaveEffect(UUID enchantedPermanentId,
                                                     boolean cannotBeRegenerated) implements CardEffect {

    public DestroyEnchantedCreatureOnLeaveEffect() {
        this(null, true);
    }

    public DestroyEnchantedCreatureOnLeaveEffect(UUID enchantedPermanentId) {
        this(enchantedPermanentId, true);
    }
}
