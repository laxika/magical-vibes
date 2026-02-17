package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameHelper;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraveyardChoiceHandlerService {

    private final GameQueryService gameQueryService;
    private final GameHelper gameHelper;
    private final GameBroadcastService gameBroadcastService;
    private final TurnProgressionService turnProgressionService;

    public void handleGraveyardCardChosen(GameData gameData, Player player, int cardIndex) {
        if (gameData.awaitingInput != AwaitingInput.GRAVEYARD_CHOICE) {
            throw new IllegalStateException("Not awaiting graveyard choice");
        }
        if (!player.getId().equals(gameData.awaitingGraveyardChoicePlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        UUID playerId = player.getId();
        Set<Integer> validIndices = gameData.awaitingGraveyardChoiceValidIndices;
        List<Card> cardPool = gameData.graveyardChoiceCardPool;

        gameData.awaitingInput = null;
        gameData.awaitingGraveyardChoicePlayerId = null;
        gameData.awaitingGraveyardChoiceValidIndices = null;
        GraveyardChoiceDestination destination = gameData.graveyardChoiceDestination;
        gameData.graveyardChoiceDestination = null;
        gameData.graveyardChoiceCardPool = null;

        if (cardIndex == -1) {
            // Player declined
            String logEntry = player.getUsername() + " chooses not to return a card.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines to return a card from graveyard", gameData.id, player.getUsername());
        } else {
            if (!validIndices.contains(cardIndex)) {
                throw new IllegalStateException("Invalid card index: " + cardIndex);
            }

            Card card;
            if (cardPool != null) {
                // Cross-graveyard choice: card pool contains cards from any graveyard
                card = cardPool.get(cardIndex);
                gameHelper.removeCardFromGraveyardById(gameData, card.getId());
            } else {
                // Standard choice: indices into the player's own graveyard
                List<Card> graveyard = gameData.playerGraveyards.get(playerId);
                card = graveyard.remove(cardIndex);
            }

            switch (destination) {
                case HAND -> {
                    gameData.playerHands.get(playerId).add(card);

                    String logEntry = player.getUsername() + " returns " + card.getName() + " from graveyard to hand.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} returns {} from graveyard to hand", gameData.id, player.getUsername(), card.getName());
                }
                case BATTLEFIELD -> {
                    Permanent perm = new Permanent(card);
                    gameData.playerBattlefields.get(playerId).add(perm);

                    String logEntry = player.getUsername() + " puts " + card.getName() + " from a graveyard onto the battlefield.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} puts {} from graveyard onto battlefield", gameData.id, player.getUsername(), card.getName());

                    if (card.getType() == CardType.CREATURE) {
                        gameHelper.handleCreatureEnteredBattlefield(gameData, playerId, card, null);
                    }
                    if (gameData.awaitingInput == null) {
                        gameHelper.checkLegendRule(gameData, playerId);
                    }
                }
            }
        }

        turnProgressionService.resolveAutoPass(gameData);
    }

    public void handleMultipleGraveyardCardsChosen(GameData gameData, Player player, List<UUID> cardIds) {
        if (gameData.awaitingInput != AwaitingInput.MULTI_GRAVEYARD_CHOICE) {
            throw new IllegalStateException("Not awaiting multi-graveyard choice");
        }
        if (!player.getId().equals(gameData.awaitingMultiGraveyardChoicePlayerId)) {
            throw new IllegalStateException("Not your turn to choose");
        }

        Set<UUID> validIds = gameData.awaitingMultiGraveyardChoiceValidCardIds;
        int maxCount = gameData.awaitingMultiGraveyardChoiceMaxCount;

        if (cardIds == null) {
            cardIds = List.of();
        }

        if (cardIds.size() > maxCount) {
            throw new IllegalStateException("Too many cards selected: " + cardIds.size() + " > " + maxCount);
        }

        Set<UUID> uniqueIds = new HashSet<>(cardIds);
        if (uniqueIds.size() != cardIds.size()) {
            throw new IllegalStateException("Duplicate card IDs in selection");
        }

        for (UUID cardId : cardIds) {
            if (!validIds.contains(cardId)) {
                throw new IllegalStateException("Invalid card: " + cardId);
            }
        }

        // Retrieve the pending ETB info
        Card pendingCard = gameData.pendingGraveyardTargetCard;
        UUID controllerId = gameData.pendingGraveyardTargetControllerId;
        List<CardEffect> pendingEffects = gameData.pendingGraveyardTargetEffects;

        // Clear awaiting state
        gameData.awaitingInput = null;
        gameData.awaitingMultiGraveyardChoicePlayerId = null;
        gameData.awaitingMultiGraveyardChoiceValidCardIds = null;
        gameData.awaitingMultiGraveyardChoiceMaxCount = 0;
        gameData.pendingGraveyardTargetCard = null;
        gameData.pendingGraveyardTargetControllerId = null;
        gameData.pendingGraveyardTargetEffects = null;

        // Put the ETB ability on the stack with the chosen targets
        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                pendingCard,
                controllerId,
                pendingCard.getName() + "'s ETB ability",
                new ArrayList<>(pendingEffects),
                new ArrayList<>(cardIds)
        ));

        if (cardIds.isEmpty()) {
            String etbLog = pendingCard.getName() + "'s enter-the-battlefield ability triggers targeting no cards.";
            gameBroadcastService.logAndBroadcast(gameData, etbLog);
        } else {
            List<String> targetNames = new ArrayList<>();
            for (UUID cardId : cardIds) {
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                if (card != null) {
                    targetNames.add(card.getName());
                }
            }
            String etbLog = pendingCard.getName() + "'s enter-the-battlefield ability triggers targeting " + String.join(", ", targetNames) + ".";
            gameBroadcastService.logAndBroadcast(gameData, etbLog);
        }
        log.info("Game {} - {} ETB ability pushed onto stack with {} graveyard targets", gameData.id, pendingCard.getName(), cardIds.size());

        turnProgressionService.resolveAutoPass(gameData);
    }
}
