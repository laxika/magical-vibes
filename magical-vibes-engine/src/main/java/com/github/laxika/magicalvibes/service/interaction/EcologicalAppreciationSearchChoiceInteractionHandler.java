package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Applies the controller's Ecological Appreciation search choice. */
@Component
@RequiredArgsConstructor
public class EcologicalAppreciationSearchChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.EcologicalAppreciationSearchChoice> {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final InputCompletionService inputCompletionService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<PendingInteraction.EcologicalAppreciationSearchChoice> handledType() {
        return PendingInteraction.EcologicalAppreciationSearchChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.EcologicalAppreciationSearchChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds == null) {
            cardIds = List.of();
        }
        if (cardIds.size() > Math.min(4, interaction.pool().size())) {
            throw new IllegalStateException("Too many cards selected (max 4)");
        }

        Set<UUID> selectedIds = new HashSet<>();
        Set<String> selectedNames = new HashSet<>();
        for (UUID cardId : cardIds) {
            if (!interaction.validCardIds().contains(cardId)) {
                throw new IllegalStateException("Invalid card ID: " + cardId);
            }
            if (!selectedIds.add(cardId)) {
                throw new IllegalStateException("Duplicate card ID: " + cardId);
            }
            Card card = findCard(interaction.pool(), cardId);
            if (!selectedNames.add(card.getName())) {
                throw new IllegalStateException("Selected cards must have different names");
            }
        }

        UUID controllerId = interaction.playerId();
        List<Card> selected = interaction.pool().stream()
                .filter(card -> selectedIds.contains(card.getId()))
                .toList();
        Set<UUID> graveyardCardIds = removeFromSourceZones(gameData, controllerId, selected);
        gameData.interaction.clearAwaitingInput();

        if (selected.size() <= 2) {
            gameData.playerDecks.computeIfAbsent(controllerId, ignored -> new ArrayList<>())
                    .addAll(selected);
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId)
                            + " shuffles the revealed Ecological Appreciation cards into their library."));
            finishResolution(gameData);
            return;
        }

        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(controllerId))
                .findFirst()
                .orElse(null);
        if (opponentId == null) {
            gameData.playerDecks.computeIfAbsent(controllerId, ignored -> new ArrayList<>())
                    .addAll(selected);
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            finishResolution(gameData);
            return;
        }

        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId) + " reveals " + selected.size()
                        + " creature cards for Ecological Appreciation."));
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.EcologicalAppreciationOpponentChoice(
                        opponentId, controllerId, selected, graveyardCardIds));
    }

    private Set<UUID> removeFromSourceZones(GameData gameData, UUID controllerId, List<Card> selected) {
        List<Card> library = gameData.playerDecks.computeIfAbsent(
                controllerId, ignored -> new ArrayList<>());
        List<Card> graveyard = gameData.playerGraveyards.computeIfAbsent(
                controllerId, ignored -> new ArrayList<>());
        Set<UUID> graveyardCardIds = new HashSet<>();
        List<Card> cardsLeavingGraveyard = new ArrayList<>();

        for (Card card : selected) {
            if (removeById(library, card.getId())) {
                continue;
            }
            if (removeById(graveyard, card.getId())) {
                graveyardCardIds.add(card.getId());
                cardsLeavingGraveyard.add(card);
                continue;
            }
            throw new IllegalStateException("Selected card is no longer in its source zone");
        }

        if (!cardsLeavingGraveyard.isEmpty()) {
            graveyardService.notifyCardsLeftGraveyard(gameData, controllerId, cardsLeavingGraveyard);
        }
        return graveyardCardIds;
    }

    private boolean removeById(List<Card> cards, UUID cardId) {
        return cards.removeIf(card -> card.getId().equals(cardId));
    }

    private Card findCard(List<Card> cards, UUID cardId) {
        return cards.stream()
                .filter(card -> card.getId().equals(cardId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Invalid card ID: " + cardId));
    }

    private void finishResolution(GameData gameData) {
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
