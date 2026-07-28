package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.AdNauseamSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Ad Nauseam's "you may repeat this process" prompt: after each revealed card the controller
 * decides whether to reveal another. Accepting performs one more reveal-and-lose-life iteration
 * and re-prompts (while the library is non-empty); declining (or an emptied library) ends the
 * resolution and resumes the game.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdNauseamRepeatChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.AdNauseamRepeatChoice> {

    private final AdNauseamSupport adNauseamSupport;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.AdNauseamRepeatChoice> handledType() {
        return PendingInteraction.AdNauseamRepeatChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.MayAbilityChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.AdNauseamRepeatChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your Ad Nauseam choice");
        }

        boolean accepted = ((InteractionAnswer.MayAbilityChosen) answer).accepted();
        UUID controllerId = interaction.playerId();
        String sourceName = interaction.sourceName();

        gameData.interaction.clearAwaitingInput();

        if (accepted) {
            adNauseamSupport.revealTopCardAndLoseLife(gameData, controllerId, sourceName);
            if (adNauseamSupport.beginRepeatPromptIfPossible(gameData, controllerId, sourceName)) {
                return;
            }
        } else {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameLogService.append(gameData, GameLog.text(playerName + " stops revealing cards (" + sourceName + ")."));
        }

        // Resumes the spell's remaining effects and auto-passes. The shared epilogue matters here:
        // the reveals lose life, so the controller may already have lost — it stops on a finished
        // game where a bare resolveAutoPass would not.
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
