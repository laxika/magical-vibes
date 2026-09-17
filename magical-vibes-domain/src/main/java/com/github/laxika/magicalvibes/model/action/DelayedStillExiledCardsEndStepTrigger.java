package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.List;
import java.util.UUID;

/** A next-end-step trigger that only triggers if at least one recorded card is still exiled. */
public record DelayedStillExiledCardsEndStepTrigger(
        UUID controllerId,
        Card sourceCard,
        UUID sourcePermanentId,
        CardEffect effect,
        List<UUID> cardIds
) implements DelayedAction {

    public DelayedStillExiledCardsEndStepTrigger {
        cardIds = List.copyOf(cardIds);
    }
}
