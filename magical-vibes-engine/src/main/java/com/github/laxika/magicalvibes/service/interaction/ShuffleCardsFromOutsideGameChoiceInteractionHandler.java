package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Applies the controller's choice for Research. */
@Component
@RequiredArgsConstructor
public class ShuffleCardsFromOutsideGameChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ShuffleCardsFromOutsideGameChoice> {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.ShuffleCardsFromOutsideGameChoice> handledType() {
        return PendingInteraction.ShuffleCardsFromOutsideGameChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.ShuffleCardsFromOutsideGameChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds == null) {
            cardIds = List.of();
        }
        int maxCount = Math.min(interaction.maxCount(), interaction.pool().size());
        if (cardIds.size() > maxCount) {
            throw new IllegalStateException("Too many cards selected (max " + maxCount + ")");
        }

        Set<UUID> selectedIds = new HashSet<>();
        for (UUID cardId : cardIds) {
            if (!interaction.validCardIds().contains(cardId)) {
                throw new IllegalStateException("Invalid card ID: " + cardId);
            }
            if (!selectedIds.add(cardId)) {
                throw new IllegalStateException("Duplicate card ID: " + cardId);
            }
        }

        List<Card> selected = interaction.pool().stream()
                .filter(card -> selectedIds.contains(card.getId()))
                .toList();
        List<Card> sideboard = gameData.playerSideboards.get(interaction.playerId());
        if (!selected.isEmpty() && sideboard == null) {
            throw new IllegalStateException("Selected card is no longer outside the game");
        }
        if (sideboard != null
                && sideboard.stream().filter(card -> selectedIds.contains(card.getId())).count() != selected.size()) {
            throw new IllegalStateException("Selected card is no longer outside the game");
        }

        if (!selected.isEmpty()) {
            sideboard.removeIf(card -> selectedIds.contains(card.getId()));
            gameData.playerDecks.computeIfAbsent(interaction.playerId(), ignored -> new ArrayList<>())
                    .addAll(selected);
            LibraryShuffleHelper.shuffleLibrary(gameData, interaction.playerId());
        }

        gameData.interaction.clearAwaitingInput();
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(interaction.playerId()) + " shuffles " + selected.size()
                        + " card" + (selected.size() == 1 ? "" : "s")
                        + " from outside the game into their library."));
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
