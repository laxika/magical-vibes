package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.InteractionPromptMessage;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Handles X value choice interactions: prompts the deciding player, validates the chosen
 * value, stores it on GameData, and resumes effect resolution. The actual game logic (what
 * to do with the chosen X) lives in the effect handler that initiated the interaction
 * (e.g. PayXManaGainXLifeEffectHandler), which re-runs and reads {@code chosenXValue}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class XValueChoiceInteractionHandler implements InteractionHandler<PendingInteraction.XValueChoice> {

    private final SessionManager sessionManager;
    private final GameBroadcastService gameBroadcastService;
    private final StateBasedActionService stateBasedActionService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;
    private final EffectResolutionService effectResolutionService;

    @Override
    public Class<PendingInteraction.XValueChoice> handledType() {
        return PendingInteraction.XValueChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.NumberChosen.class;
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.XValueChoice interaction, UUID recipientId) {
        sessionManager.sendToPlayer(recipientId,
                InteractionPromptMessage.numberPick(interaction.prompt(), interaction.maxValue(), interaction.cardName()));

        String playerName = gameData.playerIdToName.get(interaction.playerId());
        log.info("Game {} - Awaiting {} to choose X value (max {})", gameData.id, playerName, interaction.maxValue());
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.XValueChoice interaction,
                             InteractionAnswer answer) {
        int chosenValue = ((InteractionAnswer.NumberChosen) answer).value();
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        int maxAllowed = interaction.maxValue();
        if (chosenValue < 0 || chosenValue > maxAllowed) {
            throw new IllegalArgumentException("X value must be between 0 and " + maxAllowed);
        }

        // Store chosen value for the effect handler to use on re-entry
        gameData.chosenXValue = chosenValue;
        gameData.interaction.clearAwaitingInput();

        // Resume effect resolution (the same effect re-runs and reads chosenXValue)
        stateBasedActionService.performStateBasedActions(gameData);

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }

        if (!gameData.interaction.isAwaitingInput()) {
            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        }
    }
}
