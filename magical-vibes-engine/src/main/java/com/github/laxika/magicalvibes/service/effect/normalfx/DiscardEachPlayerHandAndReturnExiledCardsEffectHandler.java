package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.EachPlayerHandExileReturnAtNextEndStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEachPlayerHandAndReturnExiledCardsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves the delayed discard-and-return half of Memory Jar's ability. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DiscardEachPlayerHandAndReturnExiledCardsEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardEachPlayerHandAndReturnExiledCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var delayed = (DiscardEachPlayerHandAndReturnExiledCardsEffect) effect;
        List<EachPlayerHandExileReturnAtNextEndStep.PlayerCards> players = apnapPlayers(
                gameData, delayed.players());
        for (var playerCards : players) {
            discardHand(gameData, entry, playerCards.playerId());
            returnCards(gameData, playerCards);
        }
    }

    private void discardHand(GameData gameData, StackEntry entry, UUID playerId) {
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Card> discarded = hand == null ? List.of() : new ArrayList<>(hand);
        if (hand != null) {
            hand.clear();
        }
        gameData.discardCausedByOpponent = !playerId.equals(entry.getControllerId());

        for (Card card : discarded) {
            graveyardService.discardCard(gameData, playerId, card);
            triggerCollectionService.checkDiscardTriggers(gameData, playerId, card);
        }

        String playerName = gameData.playerIdToName.get(playerId);
        if (discarded.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + " has no cards to discard (" + entry.getCard().getName() + ")."));
        } else {
            gameLogService.append(gameData, GameLog.text(playerName + " discards their hand ("
                    + discarded.size() + " card" + (discarded.size() != 1 ? "s" : "") + ") ("
                    + entry.getCard().getName() + ")."));
        }
    }

    private void returnCards(GameData gameData,
                             EachPlayerHandExileReturnAtNextEndStep.PlayerCards playerCards) {
        UUID playerId = playerCards.playerId();
        int returned = 0;
        for (UUID cardId : playerCards.cardIds()) {
            ExiledCardEntry exiled = gameData.findExiledCard(cardId);
            if (exiled == null || !playerId.equals(exiled.ownerId())
                    || !gameData.removeFromExile(cardId)) {
                continue;
            }
            gameData.addCardToHand(playerId, exiled.card());
            returned++;
        }
        if (returned > 0) {
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId)
                    + " returns " + returned + " card" + (returned != 1 ? "s" : "") + " from exile to their hand."));
        }
        log.info("Game {} - {} returns {} cards from Memory Jar's delayed exile",
                gameData.id, gameData.playerIdToName.get(playerId), returned);
    }

    private List<EachPlayerHandExileReturnAtNextEndStep.PlayerCards> apnapPlayers(
            GameData gameData, List<EachPlayerHandExileReturnAtNextEndStep.PlayerCards> players) {
        List<EachPlayerHandExileReturnAtNextEndStep.PlayerCards> order = new ArrayList<>();
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId != null) {
            players.stream()
                    .filter(player -> activePlayerId.equals(player.playerId()))
                    .findFirst()
                    .ifPresent(order::add);
        }
        players.stream()
                .filter(player -> !order.contains(player))
                .forEach(order::add);
        return order;
    }
}
