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
        RegisterDelayedReturnCurseAttachedToPlayerEffect delayed =
                (RegisterDelayedReturnCurseAttachedToPlayerEffect) effect;
        if (delayed.cardId() == null || delayed.attachedPlayerId() == null
                || !gameData.playerIds.contains(delayed.attachedPlayerId())) {
            return;
        }

        UUID ownerId = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            for (Card card : gameData.playerGraveyards.getOrDefault(playerId, java.util.List.of())) {
                if (card.getId().equals(delayed.cardId())) {
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
                    delayed.cardId(), ownerId, delayed.attachedPlayerId()));
        }
    }
}
