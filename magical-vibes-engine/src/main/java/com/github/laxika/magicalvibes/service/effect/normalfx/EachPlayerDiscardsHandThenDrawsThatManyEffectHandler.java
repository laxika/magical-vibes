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
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link EachPlayerDiscardsHandThenDrawsThatManyEffect}: in APNAP order, each player
 * discards their entire hand, then draws their discard count less the effect's fixed reduction.
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
        discardHandThenDraw(gameData, activePlayerId, entry.getControllerId(), e.drawReduction(), cardName);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                discardHandThenDraw(gameData, playerId, entry.getControllerId(), e.drawReduction(), cardName);
            }
        }
    }

    private void discardHandThenDraw(GameData gameData, UUID playerId, UUID controllerId,
            int drawReduction, String cardName) {
        String playerName = gameData.playerIdToName.get(playerId);
        List<Card> hand = gameData.playerHands.get(playerId);

        int discardCount = hand == null ? 0 : hand.size();
        if (discardCount == 0) {
            String logEntry = playerName + " has no cards to discard (" + cardName + ").";
            gameLogService.append(gameData, GameLog.text(logEntry));
            return;
        }

        List<Card> discarded = new ArrayList<>(hand);
        hand.clear();
        gameData.discardCausedByOpponent = !playerId.equals(controllerId);

        for (Card card : discarded) {
            graveyardService.discardCard(gameData, playerId, card);
            triggerCollectionService.checkDiscardTriggers(gameData, playerId, card);
        }

        String discardLog = playerName + " discards their hand (" + discardCount
                + " card" + (discardCount != 1 ? "s" : "") + ") (" + cardName + ").";
        gameLogService.append(gameData, GameLog.text(discardLog));

        int drawCount = Math.max(0, discardCount - drawReduction);
        for (int i = 0; i < drawCount; i++) {
            drawService.resolveDrawCard(gameData, playerId);
        }
        String drawLog = playerName + " draws " + drawCount + " card" + (drawCount != 1 ? "s" : "") + ".";
        gameLogService.append(gameData, GameLog.text(drawLog));
    }
}
