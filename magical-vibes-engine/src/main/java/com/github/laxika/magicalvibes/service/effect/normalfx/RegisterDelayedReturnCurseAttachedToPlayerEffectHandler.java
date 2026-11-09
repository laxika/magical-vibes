package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedReturnCurseAttachedToPlayer;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCurseAttachedToPlayerEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Registers Lynde's next-end-step return of a Curse attached to Lynde's controller. */
@Component
@RequiredArgsConstructor
public class RegisterDelayedReturnCurseAttachedToPlayerEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedReturnCurseAttachedToPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // The Curse is the card the trigger event put into a graveyard, as long as it is still that object (CR 400.7);
        // "attached to you" is the ability's controller.
        UUID cardId = entry.getTriggeringCardId();
        UUID attachedPlayerId = entry.getControllerId();
        if (cardId == null || attachedPlayerId == null || !gameData.playerIds.contains(attachedPlayerId)
                || !entry.isTriggeringCardIncarnation(gameData.graveyardEntryVersion(cardId))) {
            return;
        }

        UUID ownerId = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            for (Card card : gameData.playerGraveyards.getOrDefault(playerId, java.util.List.of())) {
                if (card.getId().equals(cardId)) {
                    ownerId = playerId;
                    break;
                }
            }
            if (ownerId != null) {
                break;
            }
        }
        if (ownerId != null) {
            gameData.queueDelayedAction(new DelayedReturnCurseAttachedToPlayer(
                    cardId, ownerId, attachedPlayerId, gameData.graveyardEntryVersion(cardId)));
        }
    }
}
