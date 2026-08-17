package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Completes a card pick from a combined library and graveyard search pool. */
@Component
@RequiredArgsConstructor
public class SearchLibraryAndOrGraveyardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.SearchLibraryAndOrGraveyardChoice> {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.SearchLibraryAndOrGraveyardChoice> handledType() {
        return PendingInteraction.SearchLibraryAndOrGraveyardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.SearchLibraryAndOrGraveyardChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        List<UUID> selectedIds = chosenIds == null ? List.of() : chosenIds;
        Set<UUID> validIds = new HashSet<>(interaction.validCardIds());
        Set<UUID> uniqueIds = new HashSet<>();
        if (selectedIds.size() > 1 || !validIds.containsAll(selectedIds)
                || selectedIds.stream().anyMatch(id -> !uniqueIds.add(id))) {
            throw new IllegalStateException("Choose at most one valid card");
        }

        UUID playerId = interaction.playerId();
        Card chosen = selectedIds.isEmpty() ? null : interaction.pool().stream()
                .filter(card -> card.getId().equals(selectedIds.getFirst()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Chosen card is no longer available"));
        boolean fromLibrary = chosen != null && interaction.libraryCardIds().contains(chosen.getId());
        if (chosen != null) {
            List<Card> zone = fromLibrary
                    ? gameData.playerDecks.getOrDefault(playerId, List.of())
                    : gameData.playerGraveyards.getOrDefault(playerId, List.of());
            boolean removed = zone.removeIf(card -> card.getId().equals(chosen.getId()));
            if (!removed) {
                throw new IllegalStateException("Chosen card is no longer in its search zone");
            }
            if (!fromLibrary) {
                graveyardService.notifyCardsLeftGraveyard(gameData, playerId, chosen);
            }
            gameData.playerHands.get(playerId).add(chosen);
            String zoneName = fromLibrary ? "library" : "graveyard";
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(playerId) + " searches their " + zoneName + ", reveals ",
                    chosen, ", and puts it into their hand."));
            if (fromLibrary) {
                LibrarySearchTriggerHelper.checkOpponentSearchTriggers(gameData, gameLogService, playerId);
                LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            }
        } else if (interaction.librarySearchAllowed()) {
            LibrarySearchTriggerHelper.checkOpponentSearchTriggers(gameData, gameLogService, playerId);
            if (gameData.playerDecks.get(playerId) != null) {
                LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            }
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " searches their library but finds no "
                            + interaction.cardLabel() + "."
                            + (gameData.playerDecks.get(playerId) == null ? "" : " Library is shuffled.")));
        }

        gameData.interaction.clearAwaitingInput();
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
