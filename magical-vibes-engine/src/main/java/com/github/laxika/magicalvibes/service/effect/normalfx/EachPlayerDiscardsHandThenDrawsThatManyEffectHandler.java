package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsHandThenDrawsThatManyEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link EachPlayerDiscardsHandThenDrawsThatManyEffect}: in APNAP order every player
 * discards their entire hand first, then, once all discards are done, each player draws their
 * discard count less the effect's fixed reduction (CR 608.2c).
 * Discards are automatic. Mirrors {@link DiscardOwnHandThenDrawThatManyEffectHandler} but applies
 * to every player.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerDiscardsHandThenDrawsThatManyEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerDiscardsHandThenDrawsThatManyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        String cardName = entry.getCard().getName();

        UUID activePlayerId = gameData.activePlayerId;
        var e = (EachPlayerDiscardsHandThenDrawsThatManyEffect) effect;

        List<UUID> apnapOrder = new ArrayList<>();
        apnapOrder.add(activePlayerId);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                apnapOrder.add(playerId);
            }
        }

        Map<UUID, Integer> discardCounts = new LinkedHashMap<>();
        for (UUID playerId : apnapOrder) {
            discardCounts.put(playerId, discardHand(gameData, playerId, entry.getControllerId(), cardName));
        }
        for (UUID playerId : apnapOrder) {
            drawForDiscard(gameData, playerId, discardCounts.get(playerId), e.drawReduction());
        }
    }

    private int discardHand(GameData gameData, UUID playerId, UUID controllerId, String cardName) {
        String playerName = gameData.playerIdToName.get(playerId);
        List<Card> hand = gameData.playerHands.get(playerId);

        int discardCount = hand == null ? 0 : hand.size();
        if (discardCount == 0) {
            String logEntry = playerName + " has no cards to discard (" + cardName + ").";
            gameLogService.append(gameData, GameLog.text(logEntry));
            return 0;
        }

        List<Card> discarded = new ArrayList<>(hand);
        hand.clear();
        gameData.discardCausedByOpponent = !playerId.equals(controllerId);

        triggerCollectionService.beginDiscardEvent(gameData, playerId);
        for (Card card : discarded) {
            graveyardService.discardCard(gameData, playerId, card);
            triggerCollectionService.checkDiscardTriggers(gameData, playerId, card);
        }
        triggerCollectionService.finishDiscardEvent(gameData);

        String discardLog = playerName + " discards their hand (" + discardCount
                + " card" + (discardCount != 1 ? "s" : "") + ") (" + cardName + ").";
        gameLogService.append(gameData, GameLog.text(discardLog));
        return discardCount;
    }

    private void drawForDiscard(GameData gameData, UUID playerId, int discardCount, int drawReduction) {
        if (discardCount == 0) {
            return;
        }
        String playerName = gameData.playerIdToName.get(playerId);
        int drawCount = Math.max(0, discardCount - drawReduction);
        for (int i = 0; i < drawCount; i++) {
            drawService.resolveDrawCard(gameData, playerId);
        }
        String drawLog = playerName + " draws " + drawCount + " card" + (drawCount != 1 ? "s" : "") + ".";
        gameLogService.append(gameData, GameLog.text(drawLog));
    }
}
