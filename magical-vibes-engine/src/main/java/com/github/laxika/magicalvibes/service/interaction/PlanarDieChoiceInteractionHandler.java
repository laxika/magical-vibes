package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.planar.PlanarDieResult;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PlanarDieChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.PlanarDieChoice> {

    private final PlanechaseService planechaseService;
    private final InputCompletionService inputCompletionService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<PendingInteraction.PlanarDieChoice> handledType() {
        return PendingInteraction.PlanarDieChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.ListChoiceMade.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.PlanarDieChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice to make");
        }

        String choice = ((InteractionAnswer.ListChoiceMade) answer).choice();
        int ignoredIndex = interaction.options().indexOf(choice);
        if (ignoredIndex < 0) {
            throw new IllegalStateException("Invalid planar die result choice");
        }

        List<PlanarDieResult> remainingRolls = new ArrayList<>(interaction.rolls());
        remainingRolls.remove(ignoredIndex);
        int remainingChoices = interaction.ignoredRollsRemaining() - 1;
        gameData.interaction.clearAwaitingInput();

        if (remainingChoices > 0) {
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.PlanarDieChoice(
                    player.getId(), remainingRolls, remainingChoices));
            return;
        }

        planechaseService.completeRoll(gameData, player.getId(), remainingRolls.getFirst());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
