package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/** Handles choosing X for a permanent's variable face-up cost. */
@Component
public class TurnFaceUpXValueChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.TurnFaceUpXValueChoice> {

    private final GameService gameService;

    @Autowired
    public TurnFaceUpXValueChoiceInteractionHandler(@Lazy GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public Class<PendingInteraction.TurnFaceUpXValueChoice> handledType() {
        return PendingInteraction.TurnFaceUpXValueChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.NumberChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.TurnFaceUpXValueChoice interaction,
                             InteractionAnswer answer) {
        int chosenValue = ((InteractionAnswer.NumberChosen) answer).value();
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }
        if (chosenValue < 0 || chosenValue > interaction.maxValue()) {
            throw new IllegalArgumentException("X value must be between 0 and " + interaction.maxValue());
        }

        gameData.interaction.clearAwaitingInput();
        gameService.completeTurnFaceUpXChoice(gameData, player, interaction.permanentId(), chosenValue);
    }
}
