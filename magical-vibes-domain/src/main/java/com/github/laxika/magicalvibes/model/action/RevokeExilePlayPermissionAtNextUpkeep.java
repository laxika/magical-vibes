package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;

/** Delayed cleanup for "until the beginning of your next upkeep, you may play that card" (e.g. Elkin
 *  Bottle). At the beginning of {@code controllerId}'s next upkeep the exiled card {@code cardId}
 *  simply loses its play permission — unlike {@link ExileToOwnerGraveyardAtNextUpkeep} it stays in
 *  exile if it was not played. Drained in {@code StepTriggerService} only when {@code controllerId}
 *  is the active player. */
public record RevokeExilePlayPermissionAtNextUpkeep(UUID controllerId, UUID cardId, Card sourceCard)
        implements DelayedAction {
}
