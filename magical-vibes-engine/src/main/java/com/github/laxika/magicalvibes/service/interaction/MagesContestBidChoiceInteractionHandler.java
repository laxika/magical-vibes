package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles numeric life bids for Mages' Contest and resumes its parked resolution. */
@Component
@RequiredArgsConstructor
public class MagesContestBidChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.MagesContestBidChoice> {

    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.MagesContestBidChoice> handledType() {
        return PendingInteraction.MagesContestBidChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.NumberChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.MagesContestBidChoice interaction,
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
