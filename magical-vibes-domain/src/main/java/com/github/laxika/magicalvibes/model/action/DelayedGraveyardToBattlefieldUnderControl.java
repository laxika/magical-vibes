package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;

import java.util.UUID;

/**
 * Delayed trigger (Seraph, Grave Betrayal, Lifeline): at the beginning of the next end step, put the
 * card with {@code cardId} from its owner's graveyard (if still there) onto the battlefield under
 * the scheduled controller's control, or under its owner's control for Lifeline.
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
 * @param returnUnderOwnersControl      when true, return the card under its owner's control
 * @param requireSourceOnBattlefield   when true nothing returns unless {@code sourcePermanentId} is
 *                                     still on the battlefield (Shirei, Shizo's Caretaker)
 * @param graveyardEntryVersion        the card's graveyard entry identity when the return was
 *                                     scheduled; a card that has since left and re-entered the
 *                                     graveyard is a new object and is not returned
 * @param triggerControllerId          the player who controls the delayed triggered ability when it
 *                                     goes on the stack; may differ from {@code controllerId}, the
 *                                     player who puts the card onto the battlefield (Endless Whispers)
 * @param optionalReturn               when true the trigger's controller may decline the return as the
 *                                     trigger resolves (Shirei, Shizo's Caretaker)
 */
public record DelayedGraveyardToBattlefieldUnderControl(
        UUID cardId,
        UUID controllerId,
        UUID sourcePermanentId,
        boolean sacrificeOnSourceControlLoss,
        CounterType counterType,
        int counterAmount,
        CardColor grantColor,
        CardSubtype grantSubtype,
        boolean returnUnderOwnersControl,
        boolean requireSourceOnBattlefield,
        long graveyardEntryVersion,
        UUID triggerControllerId,
        boolean optionalReturn
) implements DelayedAction {

    public DelayedGraveyardToBattlefieldUnderControl(
            UUID cardId, UUID controllerId, UUID sourcePermanentId, boolean sacrificeOnSourceControlLoss,
            CounterType counterType, int counterAmount, CardColor grantColor, CardSubtype grantSubtype,
            boolean returnUnderOwnersControl, boolean requireSourceOnBattlefield, long graveyardEntryVersion,
            UUID triggerControllerId) {
        this(cardId, controllerId, sourcePermanentId, sacrificeOnSourceControlLoss, counterType, counterAmount,
                grantColor, grantSubtype, returnUnderOwnersControl, requireSourceOnBattlefield,
                graveyardEntryVersion, triggerControllerId, false);
    }

    public DelayedGraveyardToBattlefieldUnderControl {
        if (triggerControllerId == null) {
            triggerControllerId = controllerId;
        }
    }

    public DelayedGraveyardToBattlefieldUnderControl(
            UUID cardId, UUID controllerId, UUID sourcePermanentId, boolean sacrificeOnSourceControlLoss,
            CounterType counterType, int counterAmount, CardColor grantColor, CardSubtype grantSubtype,
            boolean returnUnderOwnersControl, boolean requireSourceOnBattlefield, long graveyardEntryVersion) {
        this(cardId, controllerId, sourcePermanentId, sacrificeOnSourceControlLoss, counterType, counterAmount,
                grantColor, grantSubtype, returnUnderOwnersControl, requireSourceOnBattlefield,
                graveyardEntryVersion, null, false);
    }

    /** Seraph: plain return linked to the source permanent, no counters and no grants. */
    public DelayedGraveyardToBattlefieldUnderControl(UUID cardId, UUID controllerId, UUID sourcePermanentId) {
        this(cardId, controllerId, sourcePermanentId, true, null, 0, null, null, false, false, 0L, null, false);
    }
}
