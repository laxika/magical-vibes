package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FaceUpExiledCardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.FaceUpExiledCardChoice> {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.FaceUpExiledCardChoice> handledType() {
        return PendingInteraction.FaceUpExiledCardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.FaceUpExiledCardChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenIds == null || chosenIds.size() > 1
                || (chosenIds.size() == 1 && !interaction.validCardIds().contains(chosenIds.getFirst()))) {
            throw new IllegalStateException("Choose at most one face-up exiled card");
        }

        ExiledCardEntry chosen = chosenIds.isEmpty()
                ? null
                : findEligibleCard(gameData, interaction.ownerId(), chosenIds.getFirst());
        if (!chosenIds.isEmpty() && chosen == null) {
            throw new IllegalStateException("Chosen card is no longer available");
        }

        gameData.interaction.clearAwaitingInput();
        if (chosen == null) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(player.getId())
                            + " chooses not to put a face-up exiled card into its owner's graveyard."));
        } else {
            if (!gameData.removeFromExile(chosen.card().getId())) {
                throw new IllegalStateException("Chosen card is no longer available");
            }
            graveyardService.addCardToGraveyard(
                    gameData, interaction.ownerId(), chosen.card(), Zone.EXILE);
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(interaction.ownerId()) + " puts ", chosen.card(),
                    " from exile into their graveyard."));
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private ExiledCardEntry findEligibleCard(GameData gameData, UUID ownerId, UUID cardId) {
        synchronized (gameData.exiledCards) {
            for (ExiledCardEntry exiled : gameData.exiledCards) {
                if (ownerId.equals(exiled.ownerId()) && !exiled.faceDown()
                        && cardId.equals(exiled.card().getId())) {
                    return exiled;
                }
            }
        }
        return null;
    }
}
