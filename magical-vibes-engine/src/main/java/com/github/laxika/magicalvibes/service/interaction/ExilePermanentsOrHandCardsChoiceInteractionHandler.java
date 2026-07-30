package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachPlayerExilesPermanentsOrCardsFromHandEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Handles the mixed battlefield + hand exile selection of
 * {@link com.github.laxika.magicalvibes.model.effect.EachPlayerExilesPermanentsOrCardsFromHandEffect}
 * (Descent into Madness). The picks are only recorded here — the effect handler owns the APNAP
 * queue and performs the simultaneous exile once the last player has answered.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExilePermanentsOrHandCardsChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ExilePermanentsOrHandCardsChoice> {

    private final EachPlayerExilesPermanentsOrCardsFromHandEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.ExilePermanentsOrHandCardsChoice> handledType() {
        return PendingInteraction.ExilePermanentsOrHandCardsChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
            PendingInteraction.ExilePermanentsOrHandCardsChoice interaction,
            InteractionAnswer answer) {
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
