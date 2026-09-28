package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
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
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnExiledCardsToHandChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ReturnExiledCardsToHandChoice> {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.ReturnExiledCardsToHandChoice> handledType() {
        return PendingInteraction.ReturnExiledCardsToHandChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.ReturnExiledCardsToHandChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice");
        }

        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenIds == null) {
            chosenIds = List.of();
        }
        if (chosenIds.size() > interaction.maxCount()
                || chosenIds.size() != new HashSet<>(chosenIds).size()
                || chosenIds.stream().anyMatch(id -> !interaction.validCardIds().contains(id))) {
            throw new IllegalStateException("Invalid cards selected");
        }

        List<ExiledCardEntry> chosen = chosenIds.stream()
                .map(gameData::findExiledCard)
                .toList();
        if (chosen.stream().anyMatch(exiled -> exiled == null
                || !interaction.sourcePermanentId().equals(exiled.sourcePermanentId()))) {
            throw new IllegalStateException("A selected card is no longer exiled with the source");
        }

        gameData.interaction.clearAwaitingInput();
        for (ExiledCardEntry exiled : chosen) {
            if (!gameData.removeFromExile(exiled.card().getId())) {
                continue;
            }
            gameData.addCardToHand(exiled.ownerId(), exiled.card());
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(exiled.ownerId()) + " puts ", exiled.card(),
                    " from exile into their hand."));
        }
        inputCompletionService.publishStateAfterInput(gameData);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
