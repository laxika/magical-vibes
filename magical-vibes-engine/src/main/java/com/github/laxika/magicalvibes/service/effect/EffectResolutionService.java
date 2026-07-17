package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayTapPermanentsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Resolves the effects of spells and abilities as they come off the stack.
 *
 * <p>Iterates through each {@link CardEffect} on a {@link StackEntry}, delegating to the
 * appropriate {@link EffectHandler} via the {@link EffectHandlerRegistry}. Handles conditional
 * effects (e.g. metalcraft, equipped) by re-evaluating their conditions at resolution time
 * per the intervening-if-clause rule, and conditional replacement effects by selecting the
 * base or upgraded effect based on the current game state. Condition evaluation is delegated
 * to {@link ConditionEvaluationService}.</p>
 *
 * <p>Supports asynchronous resolution: when an effect requires player input (e.g. proliferate
 * choices, X value selection), resolution pauses and stores resumption state on the
 * {@link GameData} so that {@link #resolveEffectsFrom} can continue from where it left off.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EffectResolutionService {

    private final ConditionEvaluationService conditionEvaluationService;
    private final EffectHandlerRegistry registry;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport damageSupport;
    private final GameOutcomeService gameOutcomeService;
    // ObjectProvider breaks the construction-time cycle StateBasedActionService -> LegendRuleService
    // -> PlayerInputService -> (interaction handlers) -> EffectResolutionService.
    private final ObjectProvider<StateBasedActionService> stateBasedActionService;

    /**
     * Resolves all effects on the given stack entry from the beginning.
     *
     * @param gameData the current game state
     * @param entry    the stack entry whose effects should be resolved
     */
    public void resolveEffects(GameData gameData, StackEntry entry) {
        resolveEffectsFrom(gameData, entry, 0);
    }

    /**
     * Resumes resolving effects on the given stack entry starting from the specified index.
     *
     * <p>Called after an asynchronous player input (e.g. proliferate choice, X value selection)
     * completes, to continue resolving the remaining effects of the same spell or ability.
     * If another effect requires input, resolution pauses again and stores the new resumption
     * index on {@link GameData}.</p>
     *
     * @param gameData   the current game state
     * @param entry      the stack entry whose effects are being resolved
     * @param startIndex the zero-based index of the first effect to resolve
     */
    public void resolveEffectsFrom(GameData gameData, StackEntry entry, int startIndex) {
        // CR 704.3 / 104.3b — defer the player-loss state-based action until this whole resolution
        // ends (see GameData.deferPlayerLossCheck). The depth counter keeps the suppression in place
        // across nested sub-resolutions (e.g. Kinship, counter riders) so that a nested completion
        // does not re-enable the loss check for the enclosing spell/ability mid-resolution.
        gameData.effectResolutionDepth++;
        gameData.deferPlayerLossCheck = true;
        try {
            resolveEffectsLoop(gameData, entry, startIndex);
        } finally {
            gameData.effectResolutionDepth--;
            // Finalize only when unwinding the outermost frame AND the resolution is not paused for
            // player input (pendingEffectResolutionEntry set). On a pause the flag stays set so the
            // suppression survives until the resumed resolution drains and completes here.
            if (gameData.effectResolutionDepth == 0 && gameData.pendingEffectResolutionEntry == null) {
                gameData.deferPlayerLossCheck = false;
                if (gameData.currentlyResolvingControllerId == null) {
                    // Async-resumed resolution: the interaction handler that re-entered here ran the
                    // state-based check *before* resuming, so damage/counters dealt by the resumed
                    // effects (e.g. Flameblast Dragon's X, Roar of the Crowd's counted damage) have
                    // not yet been checked. Run the single-kill-site SBA now (it also covers the
                    // deferred player-loss check). Synchronous stack resolution instead runs SBA
                    // itself in StackResolutionService right after the effect list finishes, so only
                    // the player-loss check is needed here.
                    stateBasedActionService.getObject().performStateBasedActions(gameData);
                } else {
                    gameOutcomeService.checkWinCondition(gameData);
                }
            }
        }
    }

    private void resolveEffectsLoop(GameData gameData, StackEntry entry, int startIndex) {
        List<CardEffect> effects = entry.getEffectsToResolve();
        for (int i = startIndex; i < effects.size(); i++) {
            CardEffect effect = effects.get(i);
            CardEffect effectToResolve = effect;

            // Resolution-time conditions that depend on the target (e.g. TargetPermanentMatches)
            // must see this effect's group target, not the raw entry.targetId — for a multi-target
            // spell the target lives in the flat targetIds list and is only remapped below. Build
            // the condition context against the group's chosen target up front.
            ConditionContext conditionContext = ConditionContext.forStackEntry(entry);
            if (entry.getCard().getEffectTargetIndex(effect) >= 0) {
                List<UUID> conditionTargets = entry.targetsForEffect(effect);
                if (!conditionTargets.isEmpty()) {
                    conditionContext = conditionContext.withTargetId(conditionTargets.getFirst());
                }
            }

            // Conditional wrapper: re-check condition at resolution time (intervening-if)
            if (effect instanceof ConditionalEffect conditional) {
                if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                        conditionContext)) {
                    String logEntry = entry.getCard().getName() + "'s " + conditional.conditionName()
                            + " ability does nothing (" + conditional.conditionNotMetReason() + ").";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                    log.info("Game {} - {} condition no longer met for {}", gameData.id,
                            conditional.conditionName(), entry.getCard().getName());
                    continue;
                }
                effectToResolve = conditional.wrapped();
            } else if (effect instanceof ConditionalReplacementEffect replacement) {
                effectToResolve = conditionEvaluationService.isMet(gameData, replacement.condition(),
                        conditionContext)
                        ? replacement.upgradedEffect()
                        : replacement.baseEffect();
            }

            // CR 603.5 — resolution-time "you may" re-entry after player responded
            if (effectToResolve instanceof MayEffect may && gameData.resolvedMayAccepted != null) {
                boolean accepted = gameData.resolvedMayAccepted;
                gameData.resolvedMayAccepted = null;
                if (accepted) {
                    effectToResolve = may.wrapped();
                    log.info("Game {} - Player accepted may ability from {} — resolving inner effect",
                            gameData.id, entry.getCard().getName());
                } else {
                    log.info("Game {} - Player declined may ability from {} — skipping",
                            gameData.id, entry.getCard().getName());
                    continue;
                }
            }

            // CR 603.5 — resolution-time "you may pay" re-entry after player responded
            if (effectToResolve instanceof MayPayManaEffect mayPay && gameData.resolvedMayAccepted != null) {
                boolean accepted = gameData.resolvedMayAccepted;
                gameData.resolvedMayAccepted = null;
                if (accepted) {
                    effectToResolve = mayPay.wrapped();
                    log.info("Game {} - Player accepted may-pay ability from {} — resolving inner effect",
                            gameData.id, entry.getCard().getName());
                } else {
                    log.info("Game {} - Player declined may-pay ability from {} — skipping",
                            gameData.id, entry.getCard().getName());
                    continue;
                }
            }

            if (effectToResolve instanceof MayPayTapPermanentsEffect mayPayTap && gameData.resolvedMayAccepted != null) {
                boolean accepted = gameData.resolvedMayAccepted;
                gameData.resolvedMayAccepted = null;
                if (accepted) {
                    effectToResolve = mayPayTap.wrapped();
                    log.info("Game {} - Player accepted may-tap ability from {} — resolving inner effect",
                            gameData.id, entry.getCard().getName());
                } else {
                    log.info("Game {} - Player declined may-tap ability from {} — skipping",
                            gameData.id, entry.getCard().getName());
                    continue;
                }
            }

            // Multi-target support: set entry.targetId to this effect's group target based on
            // the Card's SpellTarget declarations (StackEntry.targetsForEffect slices the flat
            // target list by group). Single-target handlers read the remapped targetId; handlers
            // that support several targets per group consult targetsForEffect themselves.
            int targetIdx = entry.getCard().getEffectTargetIndex(effect);
            UUID savedTargetId = entry.getTargetId();
            if (targetIdx >= 0) {
                List<UUID> groupTargets = entry.targetsForEffect(effect);
                if (!groupTargets.isEmpty()) {
                    entry.setTargetId(groupTargets.getFirst());
                }
            }

            EffectHandler handler = registry.getHandler(effectToResolve);
            if (handler != null) {
                handler.resolve(gameData, entry, effectToResolve);
            } else {
                log.warn("No handler for effect: {}", effectToResolve.getClass().getSimpleName());
            }

            // Restore original targetId after multi-target override
            if (targetIdx >= 0) {
                entry.setTargetId(savedTargetId);
            }

            if (gameData.interaction.isAwaitingInput() || !gameData.pendingMayAbilities.isEmpty()) {
                // Store state for resumption after async input completes.
                // X_VALUE_CHOICE and resolution-time MayEffect re-run the same effect on re-entry.
                boolean rerunCurrentEffect = gameData.interaction.activeInteraction(PendingInteraction.XValueChoice.class) != null
                        || gameData.resolvingMayEffectFromStack
                        || gameData.rerunCurrentEffectAfterInteraction;
                gameData.pendingEffectResolutionEntry = entry;
                gameData.pendingEffectResolutionIndex = rerunCurrentEffect ? i : i + 1;
                return;
            }
        }
        gameData.pendingEffectResolutionEntry = null;
        gameData.pendingEffectResolutionIndex = 0;
        // Cast-time mana snapshots (converge, colors spent) live until resolution truly finishes.
        // They must survive an async pause (e.g. a "you may" that re-runs a ColorSpentToCast
        // ConditionalEffect on resume, like Cankerous Thirst); StackResolutionService only clears
        // them when resolution is not paused, so clear here once the effect loop has fully drained.
        if (entry.getCard() != null) {
            gameData.clearSpellCastConvergeValue(entry.getCard().getId());
            gameData.clearSpellCastColorsSpent(entry.getCard().getId());
        }
        // Lethally-damaged creatures die at the state-based action check that follows this
        // resolution (CR 704.5g/704.5h) — damage handlers only record marked damage.
        permanentRemovalService.removeOrphanedAuras(gameData);
        // Now that the whole resolution's damage is dealt, queue any "whenever a [color] source deals
        // damage" reflections (Justice) once per source with the summed total (CR ruling).
        damageSupport.flushSourceDamageReflections(gameData);
    }
}
