package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Delayed trigger for "at the beginning of your next end step, put that card onto the battlefield
 * under your control" (Desert Warfare). Unlike {@link DelayedGraveyardToBattlefieldUnderControl} it
 * is processed only at the end step of {@code controllerId}, and it goes on the stack when it
 * triggers so it can be responded to.
 *
 * @param cardId                the graveyard card scheduled to return
 * @param controllerId          the player whose next end step triggers the return and who controls the
 *                              returned card
 * @param graveyardEntryVersion the graveyard entry version of {@code cardId} when the trigger was
 *                              created; a card that left and re-entered the graveyard is a new object
 *                              and does not return
 * @param sourceCard            the card whose ability scheduled the return
 * @param sourcePermanentId     the permanent whose ability scheduled the return
 */
public record DelayedControllerEndStepGraveyardReturn(
        UUID cardId,
        UUID controllerId,
        long graveyardEntryVersion,
        Card sourceCard,
        UUID sourcePermanentId
) implements DelayedAction {
}
