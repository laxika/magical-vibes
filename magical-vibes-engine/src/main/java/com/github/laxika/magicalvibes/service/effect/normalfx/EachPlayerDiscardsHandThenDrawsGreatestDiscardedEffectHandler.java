package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsHandThenDrawsGreatestDiscardedEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link EachPlayerDiscardsHandThenDrawsGreatestDiscardedEffect}: every player discards
 * their entire hand in APNAP order, and only once all hands are gone does each player draw a number
 * of cards equal to the largest hand discarded this way. Discards are automatic (no player choice),
 * and the shared draw count is fixed before any draw happens, so cards drawn by an earlier player
 * never change what a later one draws.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerDiscardsHandThenDrawsGreatestDiscardedEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameLogService gameLogService;
    private final DiscardHandEffectHandler discardHandEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerDiscardsHandThenDrawsGreatestDiscardedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        String cardName = entry.getCard().getName();
        List<UUID> order = apnapOrder(gameData);

        int greatest = 0;
        for (UUID playerId : order) {
            greatest = Math.max(greatest, discardHandEffectHandler.discardHand(gameData, playerId, entry.getControllerId(), cardName));
        }

        if (greatest == 0) {
            return;
        }

        for (UUID playerId : order) {
            drawService.resolveDrawCards(gameData, playerId, greatest);
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(playerName + " draws " + greatest
                    + " card" + (greatest != 1 ? "s" : "") + " (" + cardName + ")."));
        }
        log.info("Game {} - each player drew {} cards for {}", gameData.id, greatest, cardName);
    }

    private List<UUID> apnapOrder(GameData gameData) {
        List<UUID> order = new ArrayList<>();
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId != null) {
            order.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                order.add(playerId);
            }
        }
        return order;
    }
}
