package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Handles the controller's multiplayer choice of which opponent chooses the nonland card. */
@Component
@RequiredArgsConstructor
public class PlarggAndNassariOpponentChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.PlarggAndNassariOpponentChoice> {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<PendingInteraction.PlarggAndNassariOpponentChoice> handledType() {
        return PendingInteraction.PlarggAndNassariOpponentChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.PermanentsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.PlarggAndNassariOpponentChoice interaction,
                             InteractionAnswer answer) {
        if (!interaction.controllerId().equals(player.getId())) {
            throw new IllegalStateException("Not the controller's choice");
        }
        List<UUID> selected = ((InteractionAnswer.PermanentsChosen) answer).permanentIds();
        if (selected == null || selected.size() != 1 || !interaction.opponentIds().contains(selected.getFirst())) {
            throw new IllegalStateException("Choose one opponent");
        }

        gameData.interaction.clearAwaitingInput();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.PlarggAndNassariCardChoice(
                selected.getFirst(), interaction.controllerId(), interaction.validCardIds(),
                interaction.maxCastCount()));
    }
}
