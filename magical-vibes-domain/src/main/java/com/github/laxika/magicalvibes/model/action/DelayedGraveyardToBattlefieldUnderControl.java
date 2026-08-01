package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;

import java.util.UUID;

/**
 * Delayed trigger (Seraph, Grave Betrayal): at the beginning of the next end step, put the card with
 * {@code cardId} from its owner's graveyard (if still there) onto the battlefield under
 * {@code controllerId}'s control.
 *
 * @param sourcePermanentId            the permanent whose ability scheduled the return
 * @param sacrificeOnSourceControlLoss when true the returned permanent is linked to
 *                                     {@code sourcePermanentId} so it is sacrificed if that player
 *                                     later loses control of the source (Seraph)
 * @param counterType                  when non-null, the permanent enters with {@code counterAmount}
 *                                     counters of this type
 * @param counterAmount                number of {@code counterType} counters to enter with
 * @param grantColor                   when non-null, permanently granted "in addition to its other colors"
 * @param grantSubtype                 when non-null, permanently granted "in addition to its other types"
 */
public record DelayedGraveyardToBattlefieldUnderControl(
        UUID cardId,
        UUID controllerId,
        UUID sourcePermanentId,
        boolean sacrificeOnSourceControlLoss,
        CounterType counterType,
        int counterAmount,
        CardColor grantColor,
        CardSubtype grantSubtype
) implements DelayedAction {

    /** Seraph: plain return linked to the source permanent, no counters and no grants. */
    public DelayedGraveyardToBattlefieldUnderControl(UUID cardId, UUID controllerId, UUID sourcePermanentId) {
        this(cardId, controllerId, sourcePermanentId, true, null, 0, null, null);
    }
}
