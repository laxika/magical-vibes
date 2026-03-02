package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EquippedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentEnteredThisTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReplacementConditionalEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EffectResolutionService {

    private final GameQueryService gameQueryService;
    private final EffectHandlerRegistry registry;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;

    public void resolveEffects(GameData gameData, StackEntry entry) {
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

            // Conditional wrapper: re-check condition at resolution time (intervening-if)
            if (effect instanceof ConditionalEffect conditional) {
                if (!evaluateCondition(gameData, entry, conditional)) {
                    String logEntry = entry.getCard().getName() + "'s " + conditional.conditionName()
                            + " ability does nothing (" + conditional.conditionNotMetReason() + ").";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} condition no longer met for {}", gameData.id,
                            conditional.conditionName(), entry.getCard().getName());
                    continue;
                }
                effectToResolve = conditional.wrapped();
            } else if (effect instanceof ReplacementConditionalEffect replacement) {
                effectToResolve = evaluateCondition(gameData, entry, replacement)
                        ? replacement.upgradedEffect()
                        : replacement.baseEffect();
            }

            EffectHandler handler = registry.getHandler(effectToResolve);
            if (handler != null) {
                handler.resolve(gameData, entry, effectToResolve);
            } else {
                log.warn("No handler for effect: {}", effectToResolve.getClass().getSimpleName());
            }
            if (gameData.interaction.isAwaitingInput() || !gameData.pendingMayAbilities.isEmpty()) {
                // Store state for resumption after async input completes.
                // X_VALUE_CHOICE re-runs the same effect (it checks chosenXValue on re-entry).
                boolean rerunCurrentEffect = gameData.interaction.isAwaitingInput(AwaitingInput.X_VALUE_CHOICE);
                gameData.pendingEffectResolutionEntry = entry;
                gameData.pendingEffectResolutionIndex = rerunCurrentEffect ? i : i + 1;
                return;
            }
        }
        gameData.pendingEffectResolutionEntry = null;
        gameData.pendingEffectResolutionIndex = 0;
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * Evaluates whether the condition of a {@link ConditionalEffect} is currently met.
     */
    private boolean evaluateCondition(GameData gameData, StackEntry entry, ConditionalEffect conditional) {
        return switch (conditional) {
            case MetalcraftConditionalEffect ignored ->
                    gameQueryService.isMetalcraftMet(gameData, entry.getControllerId());
            case EquippedConditionalEffect ignored ->
                    isSourceEquipped(gameData, entry);
            case PermanentEnteredThisTurnConditionalEffect petc ->
                    isPermanentEnteredThisTurnConditionMet(gameData, entry.getControllerId(), petc);
            default -> {
                log.warn("Unknown conditional effect type: {}", conditional.getClass().getSimpleName());
                yield false;
            }
        };
    }

    /**
     * Evaluates whether the condition of a {@link ReplacementConditionalEffect} is currently met.
     */
    private boolean evaluateCondition(GameData gameData, StackEntry entry, ReplacementConditionalEffect replacement) {
        return switch (replacement) {
            case MetalcraftReplacementEffect ignored ->
                    gameQueryService.isMetalcraftMet(gameData, entry.getControllerId());
            default -> {
                log.warn("Unknown replacement conditional effect type: {}", replacement.getClass().getSimpleName());
                yield false;
            }
        };
    }

    private boolean isSourceEquipped(GameData gameData, StackEntry entry) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) return false;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(playerId);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                if (perm.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                        && sourcePermanentId.equals(perm.getAttachedTo())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isPermanentEnteredThisTurnConditionMet(GameData gameData, UUID controllerId,
                                                           PermanentEnteredThisTurnConditionalEffect petc) {
        List<Card> entered = gameData.permanentsEnteredBattlefieldThisTurn
                .getOrDefault(controllerId, List.of());
        long matchCount = entered.stream()
                .filter(c -> gameQueryService.matchesCardPredicate(c, petc.predicate(), null))
                .count();
        return matchCount >= petc.minCount();
    }
}
