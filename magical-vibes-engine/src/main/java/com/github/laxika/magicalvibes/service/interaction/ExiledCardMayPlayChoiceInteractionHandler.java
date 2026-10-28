package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ExilePlayDuration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastQueueSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExiledCardMayPlayChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ExiledCardMayPlayChoice> {

    private final ExileSupport exileSupport;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final ExileFreeCastQueueSupport exileFreeCastQueueSupport;

    @Override
    public Class<PendingInteraction.ExiledCardMayPlayChoice> handledType() {
        return PendingInteraction.ExiledCardMayPlayChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.ExiledCardMayPlayChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice to make");
        }

        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenIds.isEmpty() && interaction.allowNoChoice()) {
            gameData.interaction.clearAwaitingInput();
            gameLogService.append(gameData,
                    GameLog.text(player.getUsername() + " declines to play a card exiled this way."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        if (chosenIds.size() != 1 || !interaction.validCardIds().contains(chosenIds.getFirst())) {
            throw new IllegalStateException("Choose exactly one card exiled this way");
        }

        UUID chosenId = chosenIds.getFirst();
        ExiledCardEntry chosen = gameData.findExiledCard(chosenId);
        if (chosen == null) {
            throw new IllegalStateException("Chosen card is no longer exiled");
        }

        if (interaction.castChosenImmediately()) {
            if (chosen.card().hasType(CardType.LAND)) {
                gameData.pendingFreeCastLandPlayIds.add(chosenId);
            }
            exileFreeCastQueueSupport.castChosenSpellsWithoutPaying(gameData, player, List.of(chosenId));
            return;
        }

        gameData.interaction.clearAwaitingInput();
        gameData.exilePlayPermissions.put(chosenId, interaction.playerId());
        String durationDescription;
        if (interaction.duration() == ExilePlayDuration.WHILE_EXILED) {
            exileSupport.grantPlayWhileExiled(gameData, chosenId, interaction.playerId());
            durationDescription = " may be cast for as long as it remains exiled.";
        } else if (interaction.duration() == ExilePlayDuration.END_OF_TURN) {
            gameData.exilePlayPermissionsExpireEndOfTurn.add(chosenId);
            durationDescription = " may be played until the end of this turn.";
        } else if (interaction.duration() == ExilePlayDuration.NEXT_END_STEP) {
            exileSupport.grantPlayUntilOwnersNextEndStep(gameData, chosenId, interaction.playerId());
            durationDescription = " may be played until its controller's next end step.";
        } else {
            exileSupport.grantPlayUntilOwnersNextTurn(gameData, chosenId, interaction.playerId());
            durationDescription = " may be played until the end of its controller's next turn.";
        }
        if (interaction.withoutPayingManaCost()) {
            gameData.exilePlayWithoutPayingManaCost.add(chosenId);
        }
        if (interaction.anyManaType()) {
            if (interaction.duration() == ExilePlayDuration.WHILE_EXILED) {
                gameData.exilePlayAnyManaTypeWhileExiled.add(chosenId);
            } else {
                gameData.exilePlayAnyManaType.add(chosenId);
            }
        }
        gameLogService.append(gameData, GameLog.cardThen(chosen.card(), durationDescription));
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
