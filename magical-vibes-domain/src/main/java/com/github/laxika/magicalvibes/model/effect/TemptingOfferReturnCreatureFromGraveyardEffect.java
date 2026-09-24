package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Tempting offer that lets each opponent return a creature card from their graveyard to the
 * battlefield; each opponent who accepts also causes the spell's controller to return one.
 */
public record TemptingOfferReturnCreatureFromGraveyardEffect(
        List<UUID> remainingOpponentIds,
        UUID abilityControllerId
) implements CardEffect {

    public TemptingOfferReturnCreatureFromGraveyardEffect {
        if (remainingOpponentIds != null) {
            remainingOpponentIds = List.copyOf(remainingOpponentIds);
        }
    }

    public TemptingOfferReturnCreatureFromGraveyardEffect() {
        this(null, null);
    }
}
