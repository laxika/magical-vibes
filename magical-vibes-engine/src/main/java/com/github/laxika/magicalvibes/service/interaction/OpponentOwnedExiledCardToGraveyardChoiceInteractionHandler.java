package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OpponentOwnedExiledCardToGraveyardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.OpponentOwnedExiledCardToGraveyardChoice> {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final InputCompletionService inputCompletionService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<PendingInteraction.OpponentOwnedExiledCardToGraveyardChoice> handledType() {
        return PendingInteraction.OpponentOwnedExiledCardToGraveyardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.OpponentOwnedExiledCardToGraveyardChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice");
        }

        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenIds == null) {
            chosenIds = List.of();
        }
        if (chosenIds.size() > 1
                || (chosenIds.size() == 1 && !interaction.validCardIds().contains(chosenIds.getFirst()))) {
            throw new IllegalStateException("Choose at most one card an opponent owns from exile");
        }

        ExiledCardEntry chosen = chosenIds.isEmpty()
                ? null
                : findEligibleCard(gameData, interaction.playerId(), chosenIds.getFirst());
        if (!chosenIds.isEmpty() && chosen == null) {
            throw new IllegalStateException("Chosen card is no longer available");
        }

        gameData.interaction.clearAwaitingInput();
        if (chosen == null) {
            if (gameData.pendingEffectResolutionEntry != null) {
                gameData.pendingEffectResolutionEntry.setEventValue(0);
            }
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(player.getId())
                            + " chooses not to put an opponent-owned exiled card into its owner's graveyard."));
        } else {
            if (!gameData.removeFromExile(chosen.card().getId())) {
                throw new IllegalStateException("Chosen card is no longer available");
            }
            graveyardService.addCardToGraveyard(
                    gameData, chosen.ownerId(), chosen.card(), Zone.EXILE);
            if (gameData.pendingEffectResolutionEntry != null) {
                gameData.pendingEffectResolutionEntry.setEventValue(1);
            }
            if (interaction.lifeGain() > 0) {
                lifeSupport.applyGainLife(gameData, interaction.playerId(), interaction.lifeGain(), null,
                        interaction.sourceCard(), interaction.sourceEntryType(), interaction.playerId());
            }
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(interaction.playerId()) + " puts ", chosen.card(),
                    " from exile into their graveyard."));
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private ExiledCardEntry findEligibleCard(GameData gameData, UUID controllerId, UUID cardId) {
        synchronized (gameData.exiledCards) {
            for (ExiledCardEntry exiled : gameData.exiledCards) {
                if (cardId.equals(exiled.card().getId()) && !exiled.faceDown()
                        && gameData.playerIds.contains(exiled.ownerId())
                        && !controllerId.equals(exiled.ownerId())) {
                    return exiled;
                }
            }
        }
        return null;
    }
}
