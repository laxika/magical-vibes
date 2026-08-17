package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnChoice;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerMayReturnCardsFromGraveyardToBattlefieldEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves independent optional graveyard-return choices for each player other than the
 * resolving controller.
 */
@Component
@RequiredArgsConstructor
public class EachOtherPlayerMayReturnCardsFromGraveyardToBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOtherPlayerMayReturnCardsFromGraveyardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachOtherPlayerMayReturnCardsFromGraveyardToBattlefieldEffect) effect;
        UUID controllerId = entry.getControllerId();

        List<UUID> players = new ArrayList<>();
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId != null && !activePlayerId.equals(controllerId)) {
            players.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId) && !playerId.equals(controllerId)) {
                players.add(playerId);
            }
        }

        for (UUID playerId : players) {
            gameData.pendingGraveyardReturnQueue.add(new PendingGraveyardReturnChoice(
                    playerId,
                    e.maxCount(),
                    e.filter(),
                    GraveyardChoiceDestination.BATTLEFIELD,
                    false));
        }
        graveyardReturnSupport.beginNextGraveyardReturnFromQueue(gameData);
    }
}
