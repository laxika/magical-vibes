package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.BrilliantUltimatumSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Applies the controller's choice between the two Brilliant Ultimatum piles. */
@Component
@RequiredArgsConstructor
public class BrilliantUltimatumPileChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.BrilliantUltimatumPileChoice> {

    private final BrilliantUltimatumSupport brilliantUltimatumSupport;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.BrilliantUltimatumPileChoice> handledType() {
        return PendingInteraction.BrilliantUltimatumPileChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.MayAbilityChosen.class;
    }

    @Override
    public void handleAnswer(
            GameData gameData,
            Player player,
            PendingInteraction.BrilliantUltimatumPileChoice interaction,
            InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your pile choice");
        }

        boolean choosePileOne = ((InteractionAnswer.MayAbilityChosen) answer).accepted();
        gameData.interaction.clearAwaitingInput();
        brilliantUltimatumSupport.completePileSeparationStep2(gameData, choosePileOne);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
