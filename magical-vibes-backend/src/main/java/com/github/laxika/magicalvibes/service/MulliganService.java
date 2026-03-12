package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LeylineStartOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.MulliganResolvedMessage;
import com.github.laxika.magicalvibes.networking.message.SelectCardsToBottomMessage;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MulliganService {

    private final Random random = new Random();

    private final SessionManager sessionManager;
    private final GameBroadcastService gameBroadcastService;
    private final TurnProgressionService turnProgressionService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final PlayerInputService playerInputService;

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
        // Leyline mechanic (CR 103.6): if a card with the leyline ability is in a player's
        // opening hand, the player may begin the game with it on the battlefield.
        // Per CR 103.6, the starting player takes all such actions first, then each other player.
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null) continue;
            for (Card card : hand) {
                for (CardEffect effect : card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL)) {
                    if (effect instanceof MayEffect may
                            && may.wrapped() instanceof LeylineStartOnBattlefieldEffect) {
                        gameData.queueMayAbility(card, playerId, may);
                    }
                }
            }
        }
        if (!gameData.pendingMayAbilities.isEmpty()) {
            gameBroadcastService.broadcastGameState(gameData);
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        continueStartGame(gameData);
    }

    /**
     * Called after all leyline may-choices have been resolved to finish the game start
     * (Karn restart, set game to RUNNING, begin turn 1).
     */
    public void continueStartGame(GameData gameData) {
        // Karn Liberated restart: put exiled cards onto battlefield before starting
        // Per ruling: "After the pregame procedure is complete but before the new game's
        // first turn, Karn's ability finishes resolving and the cards left in exile are
        // put onto the battlefield."
        if (gameData.pendingKarnRestartCards != null && !gameData.pendingKarnRestartCards.isEmpty()) {
            UUID controllerId = gameData.karnRestartControllerId;
            String controllerName = gameData.playerIdToName.get(controllerId);
            for (Card card : gameData.pendingKarnRestartCards) {
                Permanent perm = new Permanent(card);
                perm.setSummoningSick(false);
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);

                if (card.getType() == CardType.PLANESWALKER && card.getLoyalty() != null) {
                    perm.setLoyaltyCounters(card.getLoyalty());
                }

                boolean isCreature = card.getType() == CardType.CREATURE
                        || card.getAdditionalTypes().contains(CardType.CREATURE);
                if (isCreature) {
                    battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, card, null, false);
                }

                String entryLog = controllerName + " puts " + card.getName() + " onto the battlefield (Karn Liberated).";
                gameBroadcastService.logAndBroadcast(gameData, entryLog);
                log.info("Game {} - {} puts {} onto the battlefield from Karn's exile",
                        gameData.id, controllerName, card.getName());
            }
        }
        gameData.pendingKarnRestartCards = null;
        gameData.karnRestartControllerId = null;

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

