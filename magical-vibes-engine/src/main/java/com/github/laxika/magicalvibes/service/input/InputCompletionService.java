package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.WarpWorldService;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.StackResolutionService;
import com.github.laxika.magicalvibes.service.event.GameMutationCoordinator;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.ObjectProvider;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;

/**
 * Shared completion logic for input handler services.
 *
 * <p>Most input handlers end with the same epilogue: process the next pending
 * may ability, check whether further input is needed, and if not, let auto-pass
 * advance to its stable observation point before recording a state invalidation.
 * This service extracts those repeated patterns into reusable methods.
 */
@Service
@RequiredArgsConstructor
public class InputCompletionService {
    @Autowired private com.github.laxika.magicalvibes.service.CommanderZoneMoveService commanderZoneMoves;


    private final PlayerInputService playerInputService;
    private final GameMutationCoordinator mutationCoordinator;
    private final TurnProgressionService turnProgressionService;
    private final StateBasedActionService stateBasedActionService;
    private final EffectResolutionService effectResolutionService;
    @Autowired
    private InteractionHandlerRegistry interactionHandlerRegistry;
    @Autowired
    private ObjectProvider<StackResolutionService> stackResolutionService;
    @Autowired
    @Lazy
    private WarpWorldService warpWorldService;
    @Autowired
    @Lazy
    private DrawService drawService;
    @Autowired
    @Lazy
    private com.github.laxika.magicalvibes.service.PermanentAuctionService permanentAuctionService;

    /**
     * Process the next pending may ability (if any). If the queue is drained and
     * no further input is needed, clear priority passes, resolve auto-pass, and
     * invalidate player views at the resulting stable point.
     *
     * <p>This is the most common completion pattern, used by may-ability handlers,
     * penalty-choice handlers, and misc input handlers.
     */
    public void processMayAbilitiesThenAutoPass(GameData gameData) {
        processMayAbilitiesThenAutoPass(gameData, true);
    }

    /**
     * The common completion epilogue without clearing existing priority passes.
     *
     * <p>This preserves the legacy semantics of handlers that previously resumed a
     * parked resolution and called auto-pass directly.
     */
    public void processMayAbilitiesThenAutoPassPreservingPriority(GameData gameData) {
        processMayAbilitiesThenAutoPass(gameData, false);
    }

    private void processMayAbilitiesThenAutoPass(GameData gameData, boolean clearPriorityPasses) {
        if (gameData.waitingForSubgame) return;
        if (gameData.status == GameStatus.FINISHED) return;
        if (gameData.interaction.isAwaitingInput()) return;
        if (commanderZoneMoves != null && commanderZoneMoves.beginPending(gameData)) return;
        var queuedManaChoice = gameData.pendingInteractions.stream()
                .filter(pending -> pending instanceof PendingInteraction.ColorChoice choice
                        && !(choice.context() instanceof ChoiceContext.RegenerationShieldChoice))
                .findFirst().orElse(null);
        if (queuedManaChoice != null) {
            gameData.pendingInteractions.removeFirstOccurrence(queuedManaChoice);
            interactionHandlerRegistry.begin(gameData, queuedManaChoice);
            return;
        }
        if (gameData.pendingInteractions.stream().anyMatch(pending ->
                pending instanceof PendingInteraction.ColorChoice choice
                        && choice.context() instanceof ChoiceContext.RegenerationShieldChoice)) {
            stateBasedActionService.performStateBasedActions(gameData);
            if (gameData.interaction.isAwaitingInput()) return;
        }
        if (!gameData.pendingAuctionEntries.isEmpty()) {
            permanentAuctionService.resumePendingEntries(gameData);
            if (gameData.interaction.isAwaitingInput()) return;
        }
        if (!gameData.pendingCardDraws.isEmpty()) {
            drawService.resumePendingCardDraws(gameData);
            if (commanderZoneMoves != null && commanderZoneMoves.beginPending(gameData)) return;
            if (gameData.status == GameStatus.FINISHED) return;
            if (gameData.interaction.isAwaitingInput()) return;
        }
        playerInputService.processNextMayAbility(gameData);
        if (gameData.pendingMayAbilities.isEmpty() && !gameData.interaction.isAwaitingInput()) {
            StackEntry pendingAuraEntry = gameData.interaction.consumePendingAuraResolutionEntry();
            if (pendingAuraEntry != null) {
                stackResolutionService.getObject().completeDeferredAuraResolution(gameData, pendingAuraEntry);
            }

            // Resume resolving remaining effects on the same spell/ability
            // (e.g. Ponder: after "you may shuffle" resolves, continue with "draw a card")
            if (gameData.pendingEffectResolutionEntry != null) {
                effectResolutionService.resolveEffectsFrom(gameData,
                        gameData.pendingEffectResolutionEntry,
                        gameData.pendingEffectResolutionIndex);
                if (gameData.waitingForSubgame) return;
            }

            if (!gameData.pendingMayAbilities.isEmpty() || gameData.interaction.isAwaitingInput()) {
                // resolveEffectsFrom may have queued new may-abilities — present the next one
                if (!gameData.interaction.isAwaitingInput() && !gameData.pendingMayAbilities.isEmpty()) {
                    playerInputService.processNextMayAbility(gameData);
                }
                return;
            }

            if (gameData.pendingEffectResolutionEntry == null && resumeWordOfCommandIfReady(gameData)) {
                if (!gameData.pendingMayAbilities.isEmpty() || gameData.interaction.isAwaitingInput()) {
                    if (!gameData.interaction.isAwaitingInput() && !gameData.pendingMayAbilities.isEmpty()) {
                        playerInputService.processNextMayAbility(gameData);
                    }
                    return;
                }
            }

            if (gameData.status == GameStatus.FINISHED) {
                return;
            }
            if (gameData.stack.isEmpty() && !gameData.pendingLibraryBottomReorders.isEmpty()) {
                warpWorldService.beginNextPendingLibraryBottomReorder(gameData);
                if (gameData.interaction.isAwaitingInput()) {
                    return;
                }
            }
            if (clearPriorityPasses) {
                gameData.priorityPassedBy.clear();
            }
            turnProgressionService.resolveAutoPass(gameData);
            publishStateAfterInput(gameData);
        }
    }

    /** Resumes Word of Command after the selected card's action and any resulting spell resolve. */
    private boolean resumeWordOfCommandIfReady(GameData gameData) {
        StackEntry pendingEntry = gameData.wordOfCommandPendingResolutionEntry;
        if (pendingEntry == null || gameData.wordOfCommandCastingCard) {
            return false;
        }
        if (gameData.wordOfCommandAwaitingCardResolution
                && gameData.stack.stream().anyMatch(entry -> entry.getCard() != null
                && gameData.wordOfCommandCardId != null
                && gameData.wordOfCommandCardId.equals(entry.getCard().getId()))) {
            return false;
        }

        gameData.wordOfCommandAwaitingCardResolution = false;
        gameData.wordOfCommandPendingResolutionEntry = null;
        gameData.wordOfCommandCardId = null;
        if (gameData.wordOfCommandControllerPlayerId != null
                && gameData.wordOfCommandControllerPlayerId.equals(gameData.mindControllerPlayerId)
                && gameData.wordOfCommandControlledPlayerId != null
                && gameData.wordOfCommandControlledPlayerId.equals(gameData.mindControlledPlayerId)) {
            gameData.mindControllerPlayerId = null;
            gameData.mindControlledPlayerId = null;
            gameData.mindControlUntilEndOfCombat = false;
        }
        gameData.wordOfCommandControllerPlayerId = null;
        gameData.wordOfCommandControlledPlayerId = null;
        gameData.pendingEffectResolutionEntry = pendingEntry;
        gameData.pendingEffectResolutionIndex = gameData.wordOfCommandPendingResolutionIndex;
        gameData.wordOfCommandPendingResolutionIndex = 0;
        effectResolutionService.resolveEffectsFrom(gameData, pendingEntry,
                gameData.pendingEffectResolutionIndex);
        return true;
    }

    /**
     * Records the post-answer state observation for a completed input branch.
     *
     * <p>Chained workflows call this at their legacy observation point around opening
     * the next interaction. The non-coalescible decision event remains an ordering
     * barrier on either side. Validation failures and finished games deliberately never
     * reach this epilogue.
     */
    public void publishStateAfterInput(GameData gameData) {
        if (gameData.status != GameStatus.FINISHED) {
            mutationCoordinator.invalidateAllPlayerViews(gameData);
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
     * one and stop. Otherwise, resume any effect resolution parked for the completed
     * input, then resolve auto-pass and invalidate player views once resolution is idle.
     *
     * <p>Unlike {@link #sbaProcessMayAbilitiesThenAutoPass}, this variant does NOT
     * clear priority passes before auto-pass. Used by multi-permanent and
     * battlefield handlers during mid-resolution processing.
     */
    public void sbaProcessMayAbilitiesThenAutoPassPreservingPriority(GameData gameData) {
        stateBasedActionService.performStateBasedActions(gameData);
        if (gameData.status == GameStatus.FINISHED) return;
        processMayAbilitiesThenAutoPass(gameData, false);
    }

    /**
     * Complete a mana-ability recipient choice without resuming a parked effect resolution.
     *
     * <p>The choice is part of mana-ability activation and retains the legacy priority semantics.
     * A concurrently parked entry belongs to the surrounding may-pay workflow, which resumes it
     * itself. State is still observed only after SBA and auto-pass reach their stable point.
     */
    public void sbaThenAutoPassWithoutResumingParkedResolution(GameData gameData) {
        stateBasedActionService.performStateBasedActions(gameData);
        if (gameData.status == GameStatus.FINISHED) return;
        if (gameData.interaction.isAwaitingInput()) return;
        gameData.priorityPassedBy.clear();
        turnProgressionService.resolveAutoPass(gameData);
        publishStateAfterInput(gameData);
    }
}
