package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.MulliganResolvedMessage;
import com.github.laxika.magicalvibes.networking.message.SelectCardsToBottomMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MulliganService {

    private final Random random = new Random();

    private final SessionManager sessionManager;
    private final GameBroadcastService gameBroadcastService;
    private final TurnProgressionService turnProgressionService;

    public void keepHand(GameData gameData, Player player) {
        if (gameData.playerKeptHand.contains(player.getId())) {
            throw new IllegalStateException("You have already kept your hand");
        }

        gameData.playerKeptHand.add(player.getId());
        int mulliganCount = gameData.mulliganCounts.getOrDefault(player.getId(), 0);
        List<Card> hand = gameData.playerHands.get(player.getId());

        sessionManager.sendToPlayers(gameData.orderedPlayerIds, new MulliganResolvedMessage(player.getUsername(), true, mulliganCount));

        if (mulliganCount > 0 && !hand.isEmpty()) {
            int cardsToBottom = Math.min(mulliganCount, hand.size());
            gameData.playerNeedsToBottom.put(player.getId(), cardsToBottom);

            String logEntry = player.getUsername() + " keeps their hand and must put " + cardsToBottom +
                    " card" + (cardsToBottom > 1 ? "s" : "") + " on the bottom of their library.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

            log.info("Game {} - {} kept hand, needs to bottom {} cards (mulligan count: {})", gameData.id, player.getUsername(), cardsToBottom, mulliganCount);

            gameBroadcastService.broadcastGameState(gameData);
            sessionManager.sendToPlayer(player.getId(), new SelectCardsToBottomMessage(cardsToBottom));
        } else {
            String logEntry = player.getUsername() + " keeps their hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);

            log.info("Game {} - {} kept hand (no mulligans)", gameData.id, player.getUsername());

            checkStartGame(gameData);
        }
    }

    public void bottomCards(GameData gameData, Player player, List<Integer> cardIndices) {
        Integer neededCount = gameData.playerNeedsToBottom.get(player.getId());
        if (neededCount == null) {
            throw new IllegalStateException("You don't need to put cards on the bottom");
        }
        if (cardIndices.size() != neededCount) {
            throw new IllegalStateException("You must select exactly " + neededCount + " card(s) to put on the bottom");
        }

        List<Card> hand = gameData.playerHands.get(player.getId());
        List<Card> deck = gameData.playerDecks.get(player.getId());

        Set<Integer> uniqueIndices = new HashSet<>(cardIndices);
        if (uniqueIndices.size() != cardIndices.size()) {
            throw new IllegalStateException("Duplicate card indices are not allowed");
        }
        for (int idx : cardIndices) {
            if (idx < 0 || idx >= hand.size()) {
                throw new IllegalStateException("Invalid card index: " + idx);
            }
        }

        // Sort indices descending so removal doesn't shift earlier indices
        List<Integer> sorted = new ArrayList<>(cardIndices);
        sorted.sort(Collections.reverseOrder());
        List<Card> bottomCards = new ArrayList<>();
        for (int idx : sorted) {
            bottomCards.add(hand.remove(idx));
        }
        deck.addAll(bottomCards);

        gameData.playerNeedsToBottom.remove(player.getId());

        String logEntry = player.getUsername() + " puts " + bottomCards.size() +
                " card" + (bottomCards.size() > 1 ? "s" : "") + " on the bottom of their library (keeping " + hand.size() + " cards).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} bottomed {} cards, hand size now {}", gameData.id, player.getUsername(), bottomCards.size(), hand.size());

        gameBroadcastService.broadcastGameState(gameData);
        checkStartGame(gameData);
    }

    public void mulligan(GameData gameData, Player player) {
        if (gameData.playerKeptHand.contains(player.getId())) {
            throw new IllegalStateException("You have already kept your hand");
        }
        int currentMulliganCount = gameData.mulliganCounts.getOrDefault(player.getId(), 0);
        if (currentMulliganCount >= 7) {
            throw new IllegalStateException("Maximum mulligans reached");
        }
        List<Card> hand = gameData.playerHands.get(player.getId());
        List<Card> deck = gameData.playerDecks.get(player.getId());

        deck.addAll(hand);
        hand.clear();
        Collections.shuffle(deck, random);

        List<Card> newHand = new ArrayList<>(deck.subList(0, 7));
        deck.subList(0, 7).clear();
        gameData.playerHands.put(player.getId(), newHand);

        int newMulliganCount = currentMulliganCount + 1;
        gameData.mulliganCounts.put(player.getId(), newMulliganCount);

        sessionManager.sendToPlayers(gameData.orderedPlayerIds, new MulliganResolvedMessage(player.getUsername(), false, newMulliganCount));

        String logEntry = player.getUsername() + " takes a mulligan (mulligan #" + newMulliganCount + ").";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} mulliganed (count: {})", gameData.id, player.getUsername(), newMulliganCount);
        gameBroadcastService.broadcastGameState(gameData);
    }

    private void checkStartGame(GameData gameData) {
        if (gameData.playerKeptHand.size() >= 2 && gameData.playerNeedsToBottom.isEmpty()) {
            startGame(gameData);
        }
    }

    private void startGame(GameData gameData) {
        gameData.status = GameStatus.RUNNING;
        gameData.activePlayerId = gameData.startingPlayerId;
        gameData.turnNumber = 1;
        gameData.currentStep = TurnStep.first();

        String logEntry1 = "Mulligan phase complete!";
        String logEntry2 = "Turn 1 begins. " + gameData.playerIdToName.get(gameData.activePlayerId) + "'s turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry1);
        gameBroadcastService.logAndBroadcast(gameData, logEntry2);

        gameBroadcastService.broadcastGameState(gameData);

        log.info("Game {} - Game started! Turn 1 begins. Active player: {}", gameData.id, gameData.playerIdToName.get(gameData.activePlayerId));

        turnProgressionService.resolveAutoPass(gameData);
    }
}
