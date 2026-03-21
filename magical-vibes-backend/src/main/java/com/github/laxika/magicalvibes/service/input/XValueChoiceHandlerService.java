package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.StateBasedActionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Generic handler for X value choice interactions.
 * Validates the choice, stores it on GameData, and resumes effect resolution.
 * The actual game logic (what to do with the chosen X) lives in the effect handler
 * that initiated the interaction (e.g. LifeResolutionService).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class XValueChoiceHandlerService {

    private final GameBroadcastService gameBroadcastService;
    private final StateBasedActionService stateBasedActionService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;
    private final EffectResolutionService effectResolutionService;

    public void handleXValueChosen(GameData gameData, Player player, int chosenValue) {
        if (!gameData.interaction.isAwaitingInput(AwaitingInput.X_VALUE_CHOICE)) {
            throw new IllegalStateException("Not awaiting X value choice");
        }
        InteractionContext.XValueChoice xValueChoice = gameData.interaction.xValueChoiceContext();
        if (xValueChoice == null || !player.getId().equals(xValueChoice.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        int maxAllowed = xValueChoice.maxValue();
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
