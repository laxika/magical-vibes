package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.StateBasedActionService;
import com.github.laxika.magicalvibes.service.TurnProgressionService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Shared completion logic for input handler services.
 *
 * <p>Most input handlers end with the same epilogue: process the next pending
 * may ability, check whether further input is needed, and if not, broadcast
 * game state and let the turn advance via auto-pass. This service extracts
 * those repeated patterns into reusable methods.
 */
@Service
@RequiredArgsConstructor
public class InputCompletionService {

    private final PlayerInputService playerInputService;
    private final GameBroadcastService gameBroadcastService;
    private final TurnProgressionService turnProgressionService;
    private final StateBasedActionService stateBasedActionService;
    private final EffectResolutionService effectResolutionService;

    /**
     * Process the next pending may ability (if any). If the queue is drained and
     * no further input is needed, clear priority passes, broadcast game state,
     * and resolve auto-pass.
     *
     * <p>This is the most common completion pattern, used by may-ability handlers,
     * penalty-choice handlers, and misc input handlers.
     */
    public void processMayAbilitiesThenAutoPass(GameData gameData) {
        if (gameData.status == GameStatus.FINISHED) return;
        playerInputService.processNextMayAbility(gameData);
        if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
            // Resume resolving remaining effects on the same spell/ability
            // (e.g. Ponder: after "you may shuffle" resolves, continue with "draw a card")
            if (gameData.pendingEffectResolutionEntry != null) {
                effectResolutionService.resolveEffectsFrom(gameData,
                        gameData.pendingEffectResolutionEntry,
                        gameData.pendingEffectResolutionIndex);
            }

            if (!gameData.pendingMayAbilities.isEmpty() || gameData.interaction.isAwaitingInput()) {
                return;
            }

            gameData.priorityPassedBy.clear();
            gameBroadcastService.broadcastGameState(gameData);
            turnProgressionService.resolveAutoPass(gameData);
        }
    }

    /**
     * Perform state-based actions, then {@link #processMayAbilitiesThenAutoPass}.
     *
     * <p>Used after penalty-choice and sacrifice handlers that may change game state
     * (life totals, permanents destroyed, etc.) requiring SBA before continuation.
     */
    public void sbaProcessMayAbilitiesThenAutoPass(GameData gameData) {
        stateBasedActionService.performStateBasedActions(gameData);
        if (gameData.status == GameStatus.FINISHED) return;
        processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Perform state-based actions. If may abilities are pending, process the next
     * one and stop. Otherwise, broadcast game state and resolve auto-pass.
     *
     * <p>Unlike {@link #sbaProcessMayAbilitiesThenAutoPass}, this variant does NOT
     * clear priority passes before broadcasting. Used by multi-permanent and
     * battlefield handlers during mid-resolution processing.
     */
    public void sbaMayAbilitiesThenBroadcastAutoPass(GameData gameData) {
        stateBasedActionService.performStateBasedActions(gameData);
        if (gameData.status == GameStatus.FINISHED) return;
        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }
        gameBroadcastService.broadcastGameState(gameData);
        turnProgressionService.resolveAutoPass(gameData);
    }
}
