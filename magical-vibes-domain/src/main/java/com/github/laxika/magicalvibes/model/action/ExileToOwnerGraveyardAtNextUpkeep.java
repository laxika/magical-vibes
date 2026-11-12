package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

import com.github.laxika.magicalvibes.model.Card;

/** Delayed cleanup for "until the beginning of your next upkeep, you may play that card; if you
 *  haven't, put it into its owner's graveyard" (e.g. Grinning Totem, Three Wishes). At the beginning
 *  of {@code controllerId}'s next upkeep, if the exiled card {@code cardId} is still in exile (i.e. it
 *  was not played), it is put into {@code ownerId}'s graveyard and its play permission is removed.
 *  Drained in {@code StepTriggerService} only when {@code controllerId} is the active player. The cards one
 *  resolution of {@code sourceCard} exiled ({@code schedulingResolution}) share one delayed triggered ability;
 *  another resolution of the same source created another (CR 603.7a). */
public record ExileToOwnerGraveyardAtNextUpkeep(UUID controllerId, UUID cardId, UUID ownerId, Card sourceCard,
                                                long schedulingResolution)
        implements DelayedAction {

    public ExileToOwnerGraveyardAtNextUpkeep(UUID controllerId, UUID cardId, UUID ownerId, Card sourceCard) {
        this(controllerId, cardId, ownerId, sourceCard, 0);
    }

    @Override
    public ExileToOwnerGraveyardAtNextUpkeep scheduledBy(long schedulingResolution) {
        return new ExileToOwnerGraveyardAtNextUpkeep(controllerId, cardId, ownerId, sourceCard,
                schedulingResolution);
    }
}
