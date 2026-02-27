package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Give N poison counter(s) to the controller of the enchanted permanent.
 * <p>
 * Used by auras such as Relic Putrescence ("Whenever enchanted artifact becomes tapped,
 * its controller gets a poison counter."). The {@code affectedPlayerId} is {@code null}
 * in the card definition and gets baked in at trigger time by
 * {@code TriggerCollectionService.checkEnchantedPermanentTapTriggers}.
 */
public record GiveEnchantedPermanentControllerPoisonCountersEffect(int amount, UUID affectedPlayerId) implements CardEffect {

    public GiveEnchantedPermanentControllerPoisonCountersEffect(int amount) {
        this(amount, null);
    }
}
