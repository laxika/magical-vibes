package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftReplacementEffect;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EffectResolutionService {

    private final GameHelper gameHelper;
    private final GameQueryService gameQueryService;
    private final EffectHandlerRegistry registry;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;

    void resolveEffects(GameData gameData, StackEntry entry) {
        resolveEffectsFrom(gameData, entry, 0);
    }

    /**
     * Resume resolving effects on the given stack entry starting from the specified index.
     * Called after an async input (e.g. proliferate choice) completes to continue
     * resolving remaining effects of the same spell/ability.
     */
    public void resolveEffectsFrom(GameData gameData, StackEntry entry, int startIndex) {
        List<CardEffect> effects = entry.getEffectsToResolve();
        for (int i = startIndex; i < effects.size(); i++) {
            CardEffect effect = effects.get(i);
            CardEffect effectToResolve = effect;

            // Metalcraft intervening-if: re-check condition at resolution time
            if (effect instanceof MetalcraftConditionalEffect metalcraft) {
                if (!isMetalcraftMet(gameData, entry.getControllerId())) {
                    String logEntry = entry.getCard().getName() + "'s metalcraft ability does nothing (fewer than three artifacts).";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - Metalcraft condition no longer met for {}", gameData.id, entry.getCard().getName());
                    continue;
                }
                effectToResolve = metalcraft.wrapped();
            } else if (effect instanceof MetalcraftReplacementEffect replacement) {
                effectToResolve = isMetalcraftMet(gameData, entry.getControllerId())
                        ? replacement.metalcraftEffect()
                        : replacement.baseEffect();
            }

            EffectHandler handler = registry.getHandler(effectToResolve);
            if (handler != null) {
                handler.resolve(gameData, entry, effectToResolve);
            } else {
                log.warn("No handler for effect: {}", effectToResolve.getClass().getSimpleName());
            }
            if (gameData.interaction.isAwaitingInput() || !gameData.pendingMayAbilities.isEmpty()) {
                // Store state for resumption after async input completes
                gameData.pendingEffectResolutionEntry = entry;
                gameData.pendingEffectResolutionIndex = i + 1;
                return;
            }
        }
        gameData.pendingEffectResolutionEntry = null;
        gameData.pendingEffectResolutionIndex = 0;
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private boolean isMetalcraftMet(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;
        long artifactCount = battlefield.stream()
                .filter(gameQueryService::isArtifact)
                .count();
        return artifactCount >= 3;
    }
}
