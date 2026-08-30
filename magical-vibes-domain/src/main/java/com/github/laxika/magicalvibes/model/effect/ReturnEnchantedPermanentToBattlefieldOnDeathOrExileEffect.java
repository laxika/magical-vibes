package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Zone;

import java.util.UUID;

/**
 * Returns the permanent card that left an Aura's battlefield attachment to the battlefield from
 * the exact zone recorded when the trigger was collected.
 */
public record ReturnEnchantedPermanentToBattlefieldOnDeathOrExileEffect(
        UUID cardId,
        Zone fromZone
) implements CardEffect {

    public ReturnEnchantedPermanentToBattlefieldOnDeathOrExileEffect() {
        this(null, null);
    }
}
