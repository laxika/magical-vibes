package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles Pain's Reward's numeric life bids and resumes its parked resolution. */
@Component
@RequiredArgsConstructor
public class PainsRewardBidChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.PainsRewardBidChoice> {

    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.PainsRewardBidChoice> handledType() {
        return PendingInteraction.PainsRewardBidChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.NumberChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.PainsRewardBidChoice interaction,
                             InteractionAnswer answer) {
        int chosenValue = ((InteractionAnswer.NumberChosen) answer).value();
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to bid");
        }
        if (chosenValue < 0 || chosenValue > interaction.maxBid()) {
            throw new IllegalArgumentException(
                    "Bid must be between 0 and " + interaction.maxBid());
        }

        gameData.chosenXValue = chosenValue;
        gameData.interaction.clearAwaitingInput();
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
