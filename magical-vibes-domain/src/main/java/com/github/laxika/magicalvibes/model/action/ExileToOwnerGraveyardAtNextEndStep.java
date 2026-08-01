package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Delayed cleanup for "at the beginning of the next end step, if the player hasn't played the
 * card, they put it into their graveyard" (Elkin Lair). At the next end step, if the exiled card
 * {@code cardId} is still in exile, it is put into {@code ownerId}'s graveyard and its play
 * permission is removed. Drained in {@code StepTriggerService.handleEndStepTriggers} with no
 * active-player filter — "the next end step" is chronological, not "your next end step".
 */
public record ExileToOwnerGraveyardAtNextEndStep(UUID cardId, UUID ownerId, Card sourceCard)
        implements DelayedAction {
}
