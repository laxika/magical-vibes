package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.ReturnAurasFromGraveyardAttachedToCreaturesEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnAurasFromGraveyardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ReturnAurasFromGraveyardChoice> {

    private final ReturnAurasFromGraveyardAttachedToCreaturesEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.ReturnAurasFromGraveyardChoice> handledType() {
        return PendingInteraction.ReturnAurasFromGraveyardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.ReturnAurasFromGraveyardChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice to make");
        }
        List<UUID> chosen = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (new HashSet<>(chosen).size() != chosen.size()) {
            throw new IllegalStateException("Duplicate card ID");
        }
        for (UUID id : chosen) {
            if (!interaction.validCardIds().contains(id)) {
                throw new IllegalStateException("Invalid card ID: " + id);
            }
        }

        gameData.interaction.clearAwaitingInput();
        effectHandler.completeCardChoice(gameData, chosen, interaction);
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }
}
