package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SearchHandAndOrLibraryChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.SearchHandAndOrLibraryChoice> {

    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.SearchHandAndOrLibraryChoice> handledType() {
        return PendingInteraction.SearchHandAndOrLibraryChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.SearchHandAndOrLibraryChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> selectedIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        List<UUID> normalizedSelectedIds = selectedIds == null ? List.of() : selectedIds;
        Set<UUID> validIds = new HashSet<>(interaction.validCardIds());
        Set<UUID> uniqueIds = new HashSet<>();
        if (normalizedSelectedIds.size() > 1 || !validIds.containsAll(normalizedSelectedIds)
                || normalizedSelectedIds.stream().anyMatch(id -> !uniqueIds.add(id))) {
            throw new IllegalStateException("Choose at most one valid card");
        }

        UUID playerId = interaction.playerId();
        Card chosen = normalizedSelectedIds.isEmpty() ? null : interaction.pool().stream()
                .filter(card -> card.getId().equals(normalizedSelectedIds.getFirst()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Chosen card is no longer available"));
        boolean fromLibrary = chosen != null && interaction.libraryCardIds().contains(chosen.getId());
        boolean fromHand = chosen != null && interaction.handCardIds().contains(chosen.getId());
        if (chosen != null) {
            List<Card> zone = fromLibrary
                    ? gameData.playerDecks.getOrDefault(playerId, List.of())
                    : fromHand
                    ? gameData.playerHands.getOrDefault(playerId, List.of())
                    : List.of();
            if (!zone.removeIf(card -> card.getId().equals(chosen.getId()))) {
                throw new IllegalStateException("Chosen card is no longer in its search zone");
            }

            if (interaction.destination() == LibrarySearchDestination.BATTLEFIELD) {
                Permanent permanent = new Permanent(chosen);
                battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, permanent,
                        battlefieldEntryService.snapshotEnterTappedTypes(gameData));
                graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, playerId, permanent, chosen);
            } else {
                gameData.addCardToHand(playerId, chosen);
            }

            String zoneName = fromLibrary ? "library" : "hand";
            String destination = interaction.destination() == LibrarySearchDestination.BATTLEFIELD
                    ? "onto the battlefield" : "into their hand";
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(playerId) + " searches their " + zoneName + ", reveals ",
                    chosen, ", and puts it " + destination + "."));
            if (fromLibrary && interaction.librarySearchAllowed()) {
                LibrarySearchTriggerHelper.checkOpponentSearchTriggers(gameData, gameLogService, playerId);
                LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            }
        } else if (interaction.librarySearchAllowed() && !interaction.libraryCardIds().isEmpty()) {
            LibrarySearchTriggerHelper.checkOpponentSearchTriggers(gameData, gameLogService, playerId);
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " searches their library but finds no "
                            + interaction.cardLabel() + ". Library is shuffled."));
        }

        gameData.interaction.clearAwaitingInput();
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
