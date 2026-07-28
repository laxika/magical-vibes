package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Handles the Doomsday choice: pick up to five cards from the held-out library+graveyard pool to
 * put on top of the library (ordered via {@link PendingInteraction.LibraryReorder} when more than
 * one is kept); the rest are exiled. Card views are re-derived from the pool at prompt time.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DoomsdayChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.DoomsdayChoice> {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.DoomsdayChoice> handledType() {
        return PendingInteraction.DoomsdayChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.DoomsdayChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds == null) {
            cardIds = List.of();
        }

        List<UUID> validIds = interaction.validCardIds();
        Set<UUID> uniqueIds = new HashSet<>();
        for (UUID id : cardIds) {
            if (!validIds.contains(id)) {
                throw new IllegalStateException("Invalid card ID: " + id);
            }
            if (!uniqueIds.add(id)) {
                throw new IllegalStateException("Duplicate card ID: " + id);
            }
        }
        if (cardIds.size() > interaction.maxCount()) {
            throw new IllegalStateException("Too many cards selected (max " + interaction.maxCount() + ")");
        }

        UUID controllerId = interaction.playerId();
        String controllerName = gameData.playerIdToName.get(controllerId);

        // Split the held-out pool into the chosen cards and the rest (pool order preserved).
        Set<UUID> chosenSet = new HashSet<>(cardIds);
        List<Card> chosen = new ArrayList<>();
        List<Card> rest = new ArrayList<>();
        for (Card card : interaction.pool()) {
            if (chosenSet.contains(card.getId())) {
                chosen.add(card);
            } else {
                rest.add(card);
            }
        }

        gameData.interaction.clearAwaitingInput();

        // Exile everything not kept.
        for (Card card : rest) {
            gameData.addToExile(controllerId, card);
        }
        if (!rest.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(controllerName + " exiles " + rest.size()
                    + " card" + (rest.size() != 1 ? "s" : "") + " (Doomsday)."));
        }

        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (chosen.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(controllerName + " puts no cards on top of their library (Doomsday)."));
            finishResolution(gameData);
        } else if (chosen.size() == 1) {
            deck.addFirst(chosen.getFirst());
            gameLogService.append(gameData, GameLog.textCardText(controllerName + " puts ", chosen.getFirst(), " on top of their library (Doomsday)."));
            finishResolution(gameData);
        } else {
            gameLogService.append(gameData, GameLog.text(controllerName + " puts " + chosen.size()
                    + " cards on top of their library (Doomsday) — choosing order."));
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                    controllerId, chosen, false, controllerId,
                    "Put these cards on top of your library in any order (top to bottom)."));
        }

        log.info("Game {} - Doomsday: {} kept {} card(s), exiled {}", gameData.id, controllerName,
                chosen.size(), rest.size());
    }

    /**
     * Finishes the Doomsday resolution when no library-reorder step is needed (zero or one card
     * kept). The shared epilogue resumes the stack entry parked in
     * {@code GameData.pendingEffectResolutionEntry} and only then auto-passes, so the spell's
     * remaining effects still run and a may ability they queue is still presented.
     */
    private void finishResolution(GameData gameData) {
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
