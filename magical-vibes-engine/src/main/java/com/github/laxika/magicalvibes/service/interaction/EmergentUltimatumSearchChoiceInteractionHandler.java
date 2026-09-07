package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Applies the controller's Emergent Ultimatum library selection. */
@Component
@RequiredArgsConstructor
public class EmergentUltimatumSearchChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.EmergentUltimatumSearchChoice> {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<PendingInteraction.EmergentUltimatumSearchChoice> handledType() {
        return PendingInteraction.EmergentUltimatumSearchChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.EmergentUltimatumSearchChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds == null) {
            cardIds = List.of();
        }
        if (cardIds.size() > Math.min(3, interaction.pool().size())) {
            throw new IllegalStateException("Too many cards selected (max 3)");
        }

        Set<UUID> selectedIds = new HashSet<>();
        Set<String> selectedNames = new HashSet<>();
        for (UUID cardId : cardIds) {
            Card card = findCard(interaction.pool(), cardId);
            if (!selectedIds.add(cardId)) {
                throw new IllegalStateException("Duplicate card ID: " + cardId);
            }
            if (!selectedNames.add(card.getName())) {
                throw new IllegalStateException("Selected cards must have different names");
            }
        }

        UUID controllerId = interaction.playerId();
        List<Card> selected = interaction.pool().stream()
                .filter(card -> selectedIds.contains(card.getId()))
                .toList();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || selected.stream().anyMatch(card -> library.stream()
                .noneMatch(libraryCard -> libraryCard.getId().equals(card.getId())))) {
            throw new IllegalStateException("Selected card is no longer in the library");
        }

        library.removeIf(card -> selectedIds.contains(card.getId()));
        selected.forEach(card -> gameData.addToExile(controllerId, card));
        gameData.interaction.clearAwaitingInput();

        if (selected.isEmpty()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(controllerId))
                .findFirst()
                .orElse(null);
        if (opponentId == null) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId)
                        + " exiles " + selected.size() + " card(s) with Emergent Ultimatum."));
        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(controllerId))
                .toList();
        if (opponents.size() == 1) {
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.EmergentUltimatumOpponentChoice(
                            opponentId, controllerId, selected));
        } else {
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.EmergentUltimatumOpponentSelectionChoice(
                            controllerId, opponents, selected));
        }
    }

    private Card findCard(List<Card> cards, UUID cardId) {
        return cards.stream()
                .filter(card -> card.getId().equals(cardId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Invalid card ID: " + cardId));
    }
}
