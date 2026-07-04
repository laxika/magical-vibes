package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayTapPermanentsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        List<CardEffect> effects = entry.getEffectsToResolve();
        for (int i = startIndex; i < effects.size(); i++) {
            CardEffect effect = effects.get(i);
            CardEffect effectToResolve = effect;

            // Conditional wrapper: re-check condition at resolution time (intervening-if)
            if (effect instanceof ConditionalEffect conditional) {
                if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                        ConditionContext.forStackEntry(entry))) {
                    String logEntry = entry.getCard().getName() + "'s " + conditional.conditionName()
                            + " ability does nothing (" + conditional.conditionNotMetReason() + ").";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} condition no longer met for {}", gameData.id,
                            conditional.conditionName(), entry.getCard().getName());
                    continue;
                }
                effectToResolve = conditional.wrapped();
            } else if (effect instanceof ConditionalReplacementEffect replacement) {
                effectToResolve = conditionEvaluationService.isMet(gameData, replacement.condition(),
                        ConditionContext.forStackEntry(entry))
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

            // Multi-target support: set entry.targetId to the correct target
            // for this effect based on the Card's SpellTarget declarations.
            int targetIdx = entry.getCard().getEffectTargetIndex(effect);
            UUID savedTargetId = entry.getTargetId();
            if (targetIdx >= 0 && entry.getTargetIds() != null
                    && targetIdx < entry.getTargetIds().size()) {
                entry.setTargetId(entry.getTargetIds().get(targetIdx));
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
                        || gameData.resolvingMayEffectFromStack;
                gameData.pendingEffectResolutionEntry = entry;
                gameData.pendingEffectResolutionIndex = rerunCurrentEffect ? i : i + 1;
                return;
            }
        }
        gameData.pendingEffectResolutionEntry = null;
        gameData.pendingEffectResolutionIndex = 0;
        destroyPendingLethalDamageCreatures(gameData);
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * Destroys all creatures that took lethal damage during effect resolution.
     * Deferred to this point so that all effects on a stack entry see the full battlefield
     * before any lethally-damaged creature is removed (matching MTG state-based action timing).
     */
    private void destroyPendingLethalDamageCreatures(GameData gameData) {
        if (gameData.pendingLethalDamageDestructions.isEmpty()) return;
        for (Permanent target : gameData.pendingLethalDamageDestructions) {
            permanentRemovalService.removePermanentToGraveyard(gameData, target);
            gameBroadcastService.logAndBroadcast(gameData, target.getCard().getName() + " is destroyed.");
            log.info("Game {} - {} is destroyed", gameData.id, target.getCard().getName());
        }
        gameData.pendingLethalDamageDestructions.clear();
    }
}
