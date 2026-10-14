package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutStillExiledCardsIntoGraveyardAndCreateTokensEffect;

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

    /**
     * Stops tracking a card that left exile. A card that leaves exile is a new object (CR 400.7),
     * so it is no longer one of the cards this trigger was waiting on even if it is exiled again.
     */
    public DelayedStillExiledCardsEndStepTrigger withoutCard(UUID cardId) {
        CardEffect updatedEffect = effect;
        if (effect instanceof PutStillExiledCardsIntoGraveyardAndCreateTokensEffect graveyardEffect) {
            updatedEffect = new PutStillExiledCardsIntoGraveyardAndCreateTokensEffect(
                    graveyardEffect.cardIds().stream().filter(id -> !id.equals(cardId)).toList(),
                    graveyardEffect.tokenEffect());
        }
        return new DelayedStillExiledCardsEndStepTrigger(controllerId, sourceCard, sourcePermanentId,
                updatedEffect, cardIds.stream().filter(id -> !id.equals(cardId)).toList());
    }
}
