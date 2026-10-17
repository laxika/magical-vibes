package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Delayed cleanup for "at the beginning of the next end step, if the player hasn't played the
 * card, they put it into their graveyard" (Elkin Lair). At the next end step, if the exiled card
 * {@code cardId} is still in exile, it is put into {@code ownerId}'s graveyard and its play
 * permission is removed. Drained in {@code StepTriggerService.handleEndStepTriggers} with no
 * active-player filter — "the next end step" is chronological, not "your next end step".
 * The delayed ability is controlled by {@code controllerId}, the controller of the ability that
 * created it (CR 603.7e), which is not necessarily the player the card was exiled from.
 * Actions sharing a non-null {@code groupId} are handled by a single delayed trigger (Mnemonic Betrayal).
 */
public record ExileToOwnerGraveyardAtNextEndStep(UUID cardId, UUID ownerId, Card sourceCard,
                                                 UUID controllerId, UUID groupId)
        implements DelayedAction {

    public ExileToOwnerGraveyardAtNextEndStep(UUID cardId, UUID ownerId, Card sourceCard, UUID controllerId) {
        this(cardId, ownerId, sourceCard, controllerId, null);
    }
}
