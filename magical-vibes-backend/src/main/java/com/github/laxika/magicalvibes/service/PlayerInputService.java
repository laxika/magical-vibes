package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromGraveyardMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromHandMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseColorMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseFromRevealedHandMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseMultipleCardsFromGraveyardsMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseMultiplePermanentsMessage;
import com.github.laxika.magicalvibes.networking.message.ChoosePermanentMessage;
import com.github.laxika.magicalvibes.networking.message.MayAbilityMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerInputService {

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;

    public void beginCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt) {
        gameData.interaction.awaitingInput = AwaitingInput.CARD_CHOICE;
        gameData.interaction.awaitingCardChoicePlayerId = playerId;
        gameData.interaction.awaitingCardChoiceValidIndices = new HashSet<>(validIndices);
        gameData.interaction.context = new InteractionContext.CardChoice(
                AwaitingInput.CARD_CHOICE,
                playerId,
                new HashSet<>(validIndices),
                null
        );
        sessionManager.sendToPlayer(playerId, new ChooseCardFromHandMessage(validIndices, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card from hand", gameData.id, playerName);
    }

    public void beginTargetedCardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt, UUID targetPermanentId) {
        gameData.interaction.awaitingInput = AwaitingInput.TARGETED_CARD_CHOICE;
        gameData.interaction.awaitingCardChoicePlayerId = playerId;
        gameData.interaction.awaitingCardChoiceValidIndices = new HashSet<>(validIndices);
        gameData.interaction.pendingCardChoiceTargetPermanentId = targetPermanentId;
        gameData.interaction.context = new InteractionContext.CardChoice(
                AwaitingInput.TARGETED_CARD_CHOICE,
                playerId,
                new HashSet<>(validIndices),
                targetPermanentId
        );
        sessionManager.sendToPlayer(playerId, new ChooseCardFromHandMessage(validIndices, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card from hand (targeted)", gameData.id, playerName);
    }

    public void beginPermanentChoice(GameData gameData, UUID playerId, List<UUID> validIds, String prompt) {
        gameData.interaction.awaitingInput = AwaitingInput.PERMANENT_CHOICE;
        gameData.interaction.awaitingPermanentChoicePlayerId = playerId;
        gameData.interaction.awaitingPermanentChoiceValidIds = new HashSet<>(validIds);
        gameData.interaction.context = new InteractionContext.PermanentChoice(
                playerId,
                new HashSet<>(validIds),
                gameData.interaction.permanentChoiceContext
        );
        sessionManager.sendToPlayer(playerId, new ChoosePermanentMessage(validIds, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a permanent", gameData.id, playerName);
    }

    public void beginAnyTargetChoice(GameData gameData, UUID playerId, List<UUID> validPermanentIds, List<UUID> validPlayerIds, String prompt) {
        Set<UUID> allValidIds = new HashSet<>(validPermanentIds);
        allValidIds.addAll(validPlayerIds);
        gameData.interaction.awaitingInput = AwaitingInput.PERMANENT_CHOICE;
        gameData.interaction.awaitingPermanentChoicePlayerId = playerId;
        gameData.interaction.awaitingPermanentChoiceValidIds = allValidIds;
        gameData.interaction.context = new InteractionContext.PermanentChoice(
                playerId,
                new HashSet<>(allValidIds),
                gameData.interaction.permanentChoiceContext
        );
        sessionManager.sendToPlayer(playerId, new ChoosePermanentMessage(validPermanentIds, validPlayerIds, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose any target", gameData.id, playerName);
    }

    void beginGraveyardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt) {
        gameData.interaction.awaitingInput = AwaitingInput.GRAVEYARD_CHOICE;
        gameData.interaction.awaitingGraveyardChoicePlayerId = playerId;
        gameData.interaction.awaitingGraveyardChoiceValidIndices = new HashSet<>(validIndices);
        gameData.interaction.context = new InteractionContext.GraveyardChoice(
                playerId,
                new HashSet<>(validIndices),
                gameData.interaction.graveyardChoiceDestination,
                gameData.interaction.graveyardChoiceCardPool
        );
        sessionManager.sendToPlayer(playerId, new ChooseCardFromGraveyardMessage(validIndices, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card from graveyard", gameData.id, playerName);
    }

    void beginMultiPermanentChoice(GameData gameData, UUID playerId, List<UUID> validIds, int maxCount, String prompt) {
        gameData.interaction.awaitingInput = AwaitingInput.MULTI_PERMANENT_CHOICE;
        gameData.interaction.awaitingMultiPermanentChoicePlayerId = playerId;
        gameData.interaction.awaitingMultiPermanentChoiceValidIds = new HashSet<>(validIds);
        gameData.interaction.awaitingMultiPermanentChoiceMaxCount = maxCount;
        gameData.interaction.context = new InteractionContext.MultiPermanentChoice(
                playerId,
                new HashSet<>(validIds),
                maxCount
        );
        sessionManager.sendToPlayer(playerId, new ChooseMultiplePermanentsMessage(validIds, maxCount, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose up to {} permanents", gameData.id, playerName, maxCount);
    }

    void beginMultiGraveyardChoice(GameData gameData, UUID playerId, List<UUID> validCardIds, List<CardView> cardViews, int maxCount, String prompt) {
        gameData.interaction.awaitingInput = AwaitingInput.MULTI_GRAVEYARD_CHOICE;
        gameData.interaction.awaitingMultiGraveyardChoicePlayerId = playerId;
        gameData.interaction.awaitingMultiGraveyardChoiceValidCardIds = new HashSet<>(validCardIds);
        gameData.interaction.awaitingMultiGraveyardChoiceMaxCount = maxCount;
        gameData.interaction.context = new InteractionContext.MultiGraveyardChoice(
                playerId,
                new HashSet<>(validCardIds),
                maxCount
        );
        sessionManager.sendToPlayer(playerId, new ChooseMultipleCardsFromGraveyardsMessage(validCardIds, cardViews, maxCount, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose up to {} cards from graveyards", gameData.id, playerName, maxCount);
    }

    void beginColorChoice(GameData gameData, UUID playerId, UUID permanentId, UUID etbTargetPermanentId) {
        gameData.interaction.awaitingInput = AwaitingInput.COLOR_CHOICE;
        gameData.interaction.awaitingColorChoicePlayerId = playerId;
        gameData.interaction.awaitingColorChoicePermanentId = permanentId;
        gameData.interaction.pendingColorChoiceETBTargetId = etbTargetPermanentId;
        gameData.interaction.context = new InteractionContext.ColorChoice(
                playerId,
                permanentId,
                etbTargetPermanentId,
                gameData.interaction.colorChoiceContext
        );
        List<String> colors = List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        sessionManager.sendToPlayer(playerId, new ChooseColorMessage(colors, "Choose a color."));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a color", gameData.id, playerName);
    }

    public void beginDiscardChoice(GameData gameData, UUID playerId) {
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            validIndices.add(i);
        }

        beginDiscardChoice(gameData, playerId, validIndices, "Choose a card to discard.");
    }

    public void beginDiscardChoice(GameData gameData, UUID playerId, List<Integer> validIndices, String prompt) {
        gameData.interaction.awaitingInput = AwaitingInput.DISCARD_CHOICE;
        gameData.interaction.awaitingCardChoicePlayerId = playerId;
        gameData.interaction.awaitingCardChoiceValidIndices = new HashSet<>(validIndices);
        gameData.interaction.context = new InteractionContext.CardChoice(
                AwaitingInput.DISCARD_CHOICE,
                playerId,
                new HashSet<>(validIndices),
                null
        );
        sessionManager.sendToPlayer(playerId, new ChooseCardFromHandMessage(validIndices, prompt));

        String playerName = gameData.playerIdToName.get(playerId);
        log.info("Game {} - Awaiting {} to choose a card to discard", gameData.id, playerName);
    }

    public void beginRevealedHandChoice(GameData gameData, UUID choosingPlayerId, UUID targetPlayerId, List<Integer> validIndices, String prompt) {
        gameData.interaction.awaitingInput = AwaitingInput.REVEALED_HAND_CHOICE;
        gameData.interaction.awaitingCardChoicePlayerId = choosingPlayerId;
        gameData.interaction.awaitingCardChoiceValidIndices = new HashSet<>(validIndices);
        gameData.interaction.context = new InteractionContext.RevealedHandChoice(
                choosingPlayerId,
                targetPlayerId,
                new HashSet<>(validIndices),
                gameData.interaction.awaitingRevealedHandChoiceRemainingCount,
                gameData.interaction.awaitingRevealedHandChoiceDiscardMode,
                new ArrayList<>(gameData.interaction.awaitingRevealedHandChosenCards)
        );

        List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
        List<CardView> cardViews = targetHand.stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(choosingPlayerId, new ChooseFromRevealedHandMessage(cardViews, validIndices, prompt));

        String playerName = gameData.playerIdToName.get(choosingPlayerId);
        log.info("Game {} - Awaiting {} to choose a card from revealed hand", gameData.id, playerName);
    }

    public void processNextMayAbility(GameData gameData) {
        if (gameData.pendingMayAbilities.isEmpty()) {
            return;
        }

        PendingMayAbility next = gameData.pendingMayAbilities.getFirst();
        gameData.interaction.awaitingInput = AwaitingInput.MAY_ABILITY_CHOICE;
        gameData.interaction.awaitingMayAbilityPlayerId = next.controllerId();
        gameData.interaction.context = new InteractionContext.MayAbilityChoice(next.controllerId(), next.description());
        sessionManager.sendToPlayer(next.controllerId(), new MayAbilityMessage(next.description()));

        String playerName = gameData.playerIdToName.get(next.controllerId());
        log.info("Game {} - Awaiting {} to decide on may ability: {}", gameData.id, playerName, next.description());
    }
}

