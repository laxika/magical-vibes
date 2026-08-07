package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.PutLandsFromHandSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles The Great Aurora's per-player land-put choice: the deciding player picks any number of the
 * land cards in their hand and they all enter the battlefield untapped. Answering advances to the
 * next remaining player (APNAP order) who has a land in hand; once everyone has chosen, resolution
 * of the spell resumes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutLandsFromHandChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.PutLandsFromHandChoice> {

    private final PutLandsFromHandSupport putLandsFromHandSupport;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.PutLandsFromHandChoice> handledType() {
        return PendingInteraction.PutLandsFromHandChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.PutLandsFromHandChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> chosenCardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenCardIds == null) {
            chosenCardIds = List.of();
        }
        for (UUID id : chosenCardIds) {
            if (!interaction.validCardIds().contains(id)) {
                throw new IllegalStateException("Invalid card ID: " + id);
            }
        }

        gameData.interaction.clearAwaitingInput();
        putLandsFromHandSupport.applyPutChoice(gameData, player.getId(), chosenCardIds, interaction.cardName());
        inputCompletionService.publishStateAfterInput(gameData);

        boolean begunNext = putLandsFromHandSupport.beginNextChoice(gameData,
                interaction.remainingPlayerIds(), interaction.cardName());
        if (!begunNext) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }
}
