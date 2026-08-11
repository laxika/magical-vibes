package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Discards a card at random. If the discarded card is a creature card, each player in turn order
 * may pay the fixed life cost; if nobody pays, that card returns from its owner's graveyard to the
 * battlefield.
 *
 * <p>The extra fields are populated only by the effect handler while the payment sequence is
 * paused. Card definitions use the single-argument constructor.</p>
 *
 * @param lifeCost life payment that prevents the return
 * @param discardedCardId the discarded creature being held in the graveyard
 * @param returnControllerId the discarded card's owner/controller
 * @param remainingPayerIds players who have not yet been offered the payment
 */
public record DiscardRandomCardReturnCreatureUnlessAnyPlayerPaysLifeEffect(
        int lifeCost,
        UUID discardedCardId,
        UUID returnControllerId,
        List<UUID> remainingPayerIds
) implements CardEffect {

    public DiscardRandomCardReturnCreatureUnlessAnyPlayerPaysLifeEffect(int lifeCost) {
        this(lifeCost, null, null, List.of());
    }

    public DiscardRandomCardReturnCreatureUnlessAnyPlayerPaysLifeEffect {
        remainingPayerIds = remainingPayerIds == null ? List.of() : List.copyOf(remainingPayerIds);
    }
}
