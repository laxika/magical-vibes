package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.MayAbilityMessage;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.AdNauseamSupport;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
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

    private final SessionManager sessionManager;
    private final AdNauseamSupport adNauseamSupport;
    private final GameBroadcastService gameBroadcastService;
    private final EffectResolutionService effectResolutionService;
    private final TurnProgressionService turnProgressionService;

    @Override
    public Class<PendingInteraction.AdNauseamRepeatChoice> handledType() {
        return PendingInteraction.AdNauseamRepeatChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.MayAbilityChosen.class;
    }

    @Override
    public UUID decidingPlayerId(PendingInteraction.AdNauseamRepeatChoice interaction) {
        return interaction.playerId();
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.AdNauseamRepeatChoice interaction, UUID recipientId) {
        sessionManager.sendToPlayer(recipientId, new MayAbilityMessage(
                "Reveal the next card and lose life equal to its mana value? (" + interaction.sourceName() + ")",
                true, null));

        String playerName = gameData.playerIdToName.get(interaction.playerId());
        log.info("Game {} - Awaiting {} to decide whether to repeat {}", gameData.id, playerName, interaction.sourceName());
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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " stops revealing cards (" + sourceName + ")."));
        }

        finishResolution(gameData);
    }

    /** Resumes any remaining spell effects, then auto-passes so the spell leaves the stack. */
    private void finishResolution(GameData gameData) {
        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }
        if (!gameData.interaction.isAwaitingInput()) {
            turnProgressionService.resolveAutoPass(gameData);
        }
    }
}
