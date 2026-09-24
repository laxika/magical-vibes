package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Tempting offer that puts a counter on each creature controlled by the accepting opponent,
 * then puts the same counter on each creature controlled by the ability controller for each
 * opponent who accepted.
 *
 * <p>Opponents choose in APNAP order. The no-argument constructor is used by the card definition;
 * resolution stamps the opponent queue and controller onto the instances carried by the may
 * prompts.
 */
public record TemptingOfferPutCountersEffect(
        List<UUID> remainingOpponentIds,
        UUID abilityControllerId
) implements CardEffect {

    public TemptingOfferPutCountersEffect {
        if (remainingOpponentIds != null) {
            remainingOpponentIds = List.copyOf(remainingOpponentIds);
        }
    }

    public TemptingOfferPutCountersEffect() {
        this(null, null);
    }
}
