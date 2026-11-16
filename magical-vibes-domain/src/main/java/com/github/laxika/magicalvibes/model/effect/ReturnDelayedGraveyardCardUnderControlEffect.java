package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;

import java.util.UUID;

/**
 * Resolution of a delayed "put that card onto the battlefield under [a player's] control at the
 * beginning of the next end step" ability: it returns the card its entry names as its triggering card, if it is still
 * in the graveyard stay it was in when the ability was made (CR 400.7).
 *
 * @param returnControllerId           the player the card enters under the control of
 * @param sacrificeOnSourceControlLoss whether the returned permanent is linked to the source permanent so it is
 *                                     sacrificed when its controller loses control of the source (Seraph)
 * @param counterType                  when non-null, the returned permanent enters with {@code counterAmount} counters
 *                                     of this type
 * @param counterAmount                number of {@code counterType} counters to enter with
 * @param grantColor                   when non-null, permanently granted to the returned permanent "in addition to its
 *                                     other colors"
 * @param grantSubtype                 when non-null, permanently granted to the returned permanent "in addition to its
 *                                     other types"
 */
public record ReturnDelayedGraveyardCardUnderControlEffect(
        UUID returnControllerId,
        boolean sacrificeOnSourceControlLoss,
        CounterType counterType,
        int counterAmount,
        CardColor grantColor,
        CardSubtype grantSubtype) implements CardEffect {
}
