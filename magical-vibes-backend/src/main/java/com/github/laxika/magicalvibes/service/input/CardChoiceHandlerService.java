package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameHelper;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardChoiceHandlerService {

    private final GameQueryService gameQueryService;
    private final GameHelper gameHelper;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;

    public void handleCardChosen(GameData gameData, Player player, int cardIndex) {
        if (gameData.awaitingInput == AwaitingInput.DISCARD_CHOICE) {
            handleDiscardCardChosen(gameData, player, cardIndex);
            return;
        }

        if (gameData.awaitingInput == AwaitingInput.REVEALED_HAND_CHOICE) {
            handleRevealedHandCardChosen(gameData, player, cardIndex);
            return;
        }

        if (gameData.awaitingInput != AwaitingInput.CARD_CHOICE && gameData.awaitingInput != AwaitingInput.TARGETED_CARD_CHOICE) {
            throw new IllegalStateException("Not awaiting card choice");
        }
        if (!player.getId().equals(gameData.awaitingCardChoicePlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        UUID playerId = player.getId();
        Set<Integer> validIndices = gameData.awaitingCardChoiceValidIndices;
        boolean isTargeted = gameData.awaitingInput == AwaitingInput.TARGETED_CARD_CHOICE;

        gameData.awaitingInput = null;
        gameData.awaitingCardChoicePlayerId = null;
        gameData.awaitingCardChoiceValidIndices = null;

        UUID targetPermanentId = gameData.pendingCardChoiceTargetPermanentId;
        gameData.pendingCardChoiceTargetPermanentId = null;

        if (cardIndex == -1) {
            String logEntry = player.getUsername() + " chooses not to put a card onto the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines to put a card onto the battlefield", gameData.id, player.getUsername());
        } else {
            if (!validIndices.contains(cardIndex)) {
                throw new IllegalStateException("Invalid card index: " + cardIndex);
            }

            List<Card> hand = gameData.playerHands.get(playerId);
            Card card = hand.remove(cardIndex);

            if (isTargeted) {
                resolveTargetedCardChoice(gameData, player, playerId, hand, card, targetPermanentId);
            } else {
                resolveUntargetedCardChoice(gameData, player, playerId, hand, card);
            }
        }

        turnProgressionService.resolveAutoPass(gameData);
    }

    private void handleDiscardCardChosen(GameData gameData, Player player, int cardIndex) {
        if (!player.getId().equals(gameData.awaitingCardChoicePlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        Set<Integer> validIndices = gameData.awaitingCardChoiceValidIndices;
        if (!validIndices.contains(cardIndex)) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        Card card = hand.remove(cardIndex);

        gameHelper.addCardToGraveyard(gameData, playerId, card);

        String logEntry = player.getUsername() + " discards " + card.getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} discards {}", gameData.id, player.getUsername(), card.getName());

        gameHelper.checkDiscardTriggers(gameData, playerId);

        gameData.awaitingDiscardRemainingCount--;

        if (gameData.awaitingDiscardRemainingCount > 0 && !hand.isEmpty()) {
            playerInputService.beginDiscardChoice(gameData, playerId);
        } else {
            gameData.awaitingInput = null;
            gameData.awaitingCardChoicePlayerId = null;
            gameData.awaitingCardChoiceValidIndices = null;
            gameData.awaitingDiscardRemainingCount = 0;
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    private void handleRevealedHandCardChosen(GameData gameData, Player player, int cardIndex) {
        if (!player.getId().equals(gameData.awaitingCardChoicePlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        Set<Integer> validIndices = gameData.awaitingCardChoiceValidIndices;
        if (!validIndices.contains(cardIndex)) {
            throw new IllegalStateException("Invalid card index: " + cardIndex);
        }

        UUID targetPlayerId = gameData.awaitingRevealedHandChoiceTargetPlayerId;
        List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        Card chosenCard = targetHand.remove(cardIndex);
        gameData.awaitingRevealedHandChosenCards.add(chosenCard);

        String logEntry = player.getUsername() + " chooses " + chosenCard.getName() + " from " + targetName + "'s hand.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} chooses {} from {}'s hand", gameData.id, player.getUsername(), chosenCard.getName(), targetName);

        gameData.awaitingRevealedHandChoiceRemainingCount--;

        boolean discardMode = gameData.awaitingRevealedHandChoiceDiscardMode;

        if (gameData.awaitingRevealedHandChoiceRemainingCount > 0 && !targetHand.isEmpty()) {
            // More cards to choose — update valid indices and prompt again
            List<Integer> newValidIndices = new ArrayList<>();
            for (int i = 0; i < targetHand.size(); i++) {
                newValidIndices.add(i);
            }

            String prompt = discardMode
                    ? "Choose another card to discard."
                    : "Choose another card to put on top of " + targetName + "'s library.";
            playerInputService.beginRevealedHandChoice(gameData, player.getId(), targetPlayerId, newValidIndices, prompt);
        } else {
            // All cards chosen
            gameData.awaitingInput = null;
            gameData.awaitingCardChoicePlayerId = null;
            gameData.awaitingCardChoiceValidIndices = null;

            List<Card> chosenCards = new ArrayList<>(gameData.awaitingRevealedHandChosenCards);

            if (discardMode) {
                // Discard chosen cards to graveyard
                for (Card discarded : chosenCards) {
                    gameHelper.addCardToGraveyard(gameData, targetPlayerId, discarded);
                }

                String cardNames = String.join(", ", chosenCards.stream().map(Card::getName).toList());
                String discardLog = targetName + " discards " + cardNames + ".";
                gameBroadcastService.logAndBroadcast(gameData, discardLog);
                log.info("Game {} - {} discards {} from {}'s hand", gameData.id, player.getUsername(), cardNames, targetName);

                for (int i = 0; i < chosenCards.size(); i++) {
                    gameHelper.checkDiscardTriggers(gameData, targetPlayerId);
                }
            } else {
                // Put chosen cards on top of library
                List<Card> deck = gameData.playerDecks.get(targetPlayerId);

                // Insert in reverse order so first chosen ends up on top
                for (int i = chosenCards.size() - 1; i >= 0; i--) {
                    deck.addFirst(chosenCards.get(i));
                }

                String cardNames = String.join(", ", chosenCards.stream().map(Card::getName).toList());
                String putLog = player.getUsername() + " puts " + cardNames + " on top of " + targetName + "'s library.";
                gameBroadcastService.logAndBroadcast(gameData, putLog);
                log.info("Game {} - {} puts {} on top of {}'s library", gameData.id, player.getUsername(), cardNames, targetName);
            }

            gameData.awaitingRevealedHandChoiceTargetPlayerId = null;
            gameData.awaitingRevealedHandChoiceRemainingCount = 0;
            gameData.awaitingRevealedHandChoiceDiscardMode = false;
            gameData.awaitingRevealedHandChosenCards.clear();

            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    private void resolveTargetedCardChoice(GameData gameData, Player player, UUID playerId, List<Card> hand, Card card, UUID targetPermanentId) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
        if (target != null) {
            Permanent auraPerm = new Permanent(card);
            auraPerm.setAttachedTo(target.getId());
            gameData.playerBattlefields.get(playerId).add(auraPerm);

            String logEntry = player.getUsername() + " puts " + card.getName() + " onto the battlefield attached to " + target.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} puts {} onto the battlefield attached to {}", gameData.id, player.getUsername(), card.getName(), target.getCard().getName());
        } else {
            hand.add(card);
            String logEntry = card.getName() + " can't be attached (target left the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Aura target gone, {} returned to hand", gameData.id, card.getName());
        }
    }

    private void resolveUntargetedCardChoice(GameData gameData, Player player, UUID playerId, List<Card> hand, Card card) {
        gameData.playerBattlefields.get(playerId).add(new Permanent(card));

        String logEntry = player.getUsername() + " puts " + card.getName() + " onto the battlefield.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} puts {} onto the battlefield", gameData.id, player.getUsername(), card.getName());

        gameHelper.handleCreatureEnteredBattlefield(gameData, playerId, card, null);
    }
}
