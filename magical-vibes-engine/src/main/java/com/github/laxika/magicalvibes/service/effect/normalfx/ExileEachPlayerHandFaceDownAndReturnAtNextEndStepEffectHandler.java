package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.EachPlayerHandExileReturnAtNextEndStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileEachPlayerHandFaceDownAndReturnAtNextEndStepEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves the immediate hand-exile half of Memory Jar's ability. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileEachPlayerHandFaceDownAndReturnAtNextEndStepEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileEachPlayerHandFaceDownAndReturnAtNextEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<EachPlayerHandExileReturnAtNextEndStep.PlayerCards> players = new ArrayList<>();
        for (UUID playerId : apnapOrder(gameData)) {
            List<Card> hand = gameData.playerHands.get(playerId);
            List<Card> cards = hand == null ? List.of() : new ArrayList<>(hand);
            if (hand != null) {
                hand.clear();
            }

            for (Card card : cards) {
                exileService.exileCardFaceDown(gameData, playerId, card, null);
            }
            players.add(new EachPlayerHandExileReturnAtNextEndStep.PlayerCards(
                    playerId, cards.stream().map(Card::getId).toList()));

            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(playerName + " exiles " + cards.size()
                    + " card" + (cards.size() != 1 ? "s" : "") + " from their hand face down."));
        }

        gameData.queueDelayedAction(new EachPlayerHandExileReturnAtNextEndStep(
                entry.getCard(), entry.getControllerId(), players));
        log.info("Game {} - {} exiles every player's hand face down until the next end step",
                gameData.id, entry.getCard().getName());
    }

    private List<UUID> apnapOrder(GameData gameData) {
        List<UUID> order = new ArrayList<>();
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId != null && gameData.playerIds.contains(activePlayerId)) {
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
