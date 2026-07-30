package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.AttachAurasToSourceEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Handles the mixed battlefield + graveyard + hand Aura selection of
 * {@link com.github.laxika.magicalvibes.model.effect.AttachAurasToSourceEffect} (Bruna, Light of
 * Alabaster). The picks are forwarded to the effect handler, which performs the attachments.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttachAurasChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.AttachAurasChoice> {

    private final AttachAurasToSourceEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.AttachAurasChoice> handledType() {
        return PendingInteraction.AttachAurasChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
            PendingInteraction.AttachAurasChoice interaction, InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice to make");
        }
        List<UUID> chosen = ((InteractionAnswer.CardsChosen) answer).cardIds();
        for (UUID id : chosen) {
            if (!interaction.validCardIds().contains(id)) {
                throw new IllegalStateException("Invalid card ID: " + id);
            }
        }

        gameData.interaction.clearAwaitingInput();
        effectHandler.completeChoice(gameData, chosen, interaction);
        if (gameData.interaction.isAwaitingInput()) {
            return;
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
