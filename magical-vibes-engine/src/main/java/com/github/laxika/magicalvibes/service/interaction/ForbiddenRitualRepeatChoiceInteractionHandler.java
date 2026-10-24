package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ForbiddenRitualEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Forbidden Ritual's "you may repeat this process" prompt: after each completed cycle the
 * controller decides whether to sacrifice another nontoken permanent against the same opponent.
 * Accepting starts the next sacrifice step while nontoken permanents remain; declining ends the
 * resolution and resumes the game.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ForbiddenRitualRepeatChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ForbiddenRitualRepeatChoice> {

    private final ForbiddenRitualEffectHandler forbiddenRitualEffectHandler;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.ForbiddenRitualRepeatChoice> handledType() {
        return PendingInteraction.ForbiddenRitualRepeatChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.MayAbilityChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
            PendingInteraction.ForbiddenRitualRepeatChoice interaction, InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your Forbidden Ritual choice");
        }

        boolean accepted = ((InteractionAnswer.MayAbilityChosen) answer).accepted();
        UUID controllerId = interaction.playerId();
        String sourceName = interaction.sourceName();

        gameData.interaction.clearAwaitingInput();

        if (accepted) {
            StackEntry entry = gameData.pendingEffectResolutionEntry;
            if (entry != null) {
                // The repeat prompt parked the resolution past the Forbidden Ritual effect; resuming at that effect
                // starts the next sacrifice as the resolution step (asking a lone permanent's CR 616.1 question
                // before it leaves), and the penalty choices it starts re-enter it from there.
                gameData.pendingEffectResolutionIndex = entry.getResolvingEffectIndex();
                gameData.forbiddenRitual.repeatAccepted = true;
            } else {
                forbiddenRitualEffectHandler.finish(gameData, gameData.forbiddenRitual);
            }
        } else {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " stops repeating (" + sourceName + ")."));
            forbiddenRitualEffectHandler.finish(gameData, gameData.forbiddenRitual);
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
