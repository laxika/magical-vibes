package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.TargetPlayerChoosesCardsFromHandThenMayCastOneEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles the target player's hidden multi-pick for Extract Brain. */
@Component
@RequiredArgsConstructor
public class TargetPlayerChoosesCardsFromHandChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.TargetPlayerChoosesCardsFromHandChoice> {

    private final TargetPlayerChoosesCardsFromHandThenMayCastOneEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.TargetPlayerChoosesCardsFromHandChoice> handledType() {
        return PendingInteraction.TargetPlayerChoosesCardsFromHandChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardIndexChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.TargetPlayerChoosesCardsFromHandChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.choosingPlayerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        int cardIndex = ((InteractionAnswer.CardIndexChosen) answer).cardIndex();
        boolean complete = effectHandler.handleCardSelection(gameData, interaction, cardIndex);
        if (complete) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }
}
