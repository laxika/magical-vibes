package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ActivationCountConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.AttacksAloneConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MinimumAttackersConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlsAnotherSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlsPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlsSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DefendingPlayerPoisonedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DidntAttackConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EquippedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.NotKickedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.MorbidConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MorbidReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.NoOtherSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.NoSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentEnteredThisTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReplacementConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SourceSubtypeReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSubtypeReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
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
 * per the intervening-if-clause rule, and replacement conditional effects by selecting the
 * base or upgraded effect based on the current game state.</p>
 *
 * <p>Supports asynchronous resolution: when an effect requires player input (e.g. proliferate
 * choices, X value selection), resolution pauses and stores resumption state on the
 * {@link GameData} so that {@link #resolveEffectsFrom} can continue from where it left off.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EffectResolutionService {

    private final GameQueryService gameQueryService;
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
                boolean rerunCurrentEffect = gameData.interaction.isAwaitingInput(AwaitingInput.X_VALUE_CHOICE)
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
            case DefendingPlayerPoisonedConditionalEffect ignored ->
                    isDefendingPlayerPoisoned(gameData, entry.getControllerId());
            case ControlsAnotherSubtypeConditionalEffect cas ->
                    isControlsAnotherSubtypeConditionMet(gameData, entry, cas);
            case ControlsSubtypeConditionalEffect csc ->
                    isControlsSubtypeConditionMet(gameData, entry, csc);
            case ControlsPermanentConditionalEffect cpc ->
                    isControlsPermanentConditionMet(gameData, entry, cpc);
            case NoOtherSubtypeConditionalEffect noOther ->
                    isNoOtherSubtypeConditionMet(gameData, entry, noOther);
            case ActivationCountConditionalEffect acc ->
                    isActivationCountConditionMet(gameData, entry, acc);
            case MorbidConditionalEffect ignored ->
                    gameQueryService.isMorbidMet(gameData);
            case KickedConditionalEffect ignored ->
                    entry.isKicked();
            case NotKickedConditionalEffect ignored ->
                    !entry.isKicked();
            case DidntAttackConditionalEffect ignored ->
                    isSourceDidntAttackThisTurn(gameData, entry);
            case NoSpellsCastLastTurnConditionalEffect ignored ->
                    isNoSpellsCastLastTurn(gameData);
            case TwoOrMoreSpellsCastLastTurnConditionalEffect ignored ->
                    isTwoOrMoreSpellsCastLastTurn(gameData);
            case AttacksAloneConditionalEffect ignored ->
                    isAttackingAlone(gameData, entry);
            case MinimumAttackersConditionalEffect mac ->
                    entry.getXValue() >= mac.minimumAttackers();
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
            case MorbidReplacementEffect ignored ->
                    gameQueryService.isMorbidMet(gameData);
            case TargetSubtypeReplacementEffect tsre -> {
                Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
                yield target != null && target.getCard().getSubtypes().contains(tsre.subtype());
            }
            case SourceSubtypeReplacementEffect ssre -> {
                // Check if the source permanent has the required subtype.
                // Try the battlefield first; fall back to the card on the stack (last-known information).
                Permanent source = entry.getSourcePermanentId() != null
                        ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                        : null;
                if (source != null) {
                    yield source.getCard().getSubtypes().contains(ssre.subtype());
                }
                yield entry.getCard().getSubtypes().contains(ssre.subtype());
            }
            case KickerReplacementEffect ignored ->
                    entry.isKicked();
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

    private boolean isDefendingPlayerPoisoned(GameData gameData, UUID attackingPlayerId) {
        UUID defendingPlayerId = gameQueryService.getOpponentId(gameData, attackingPlayerId);
        return gameData.playerPoisonCounters.getOrDefault(defendingPlayerId, 0) > 0;
    }

    private boolean isControlsAnotherSubtypeConditionMet(GameData gameData, StackEntry entry,
                                                           ControlsAnotherSubtypeConditionalEffect cas) {
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;
        PermanentHasSubtypePredicate predicate = new PermanentHasSubtypePredicate(cas.subtype());
        return battlefield.stream()
                .anyMatch(p -> !p.getId().equals(sourcePermanentId)
                        && gameQueryService.matchesPermanentPredicate(gameData, p, predicate));
    }

    private boolean isControlsSubtypeConditionMet(GameData gameData, StackEntry entry,
                                                      ControlsSubtypeConditionalEffect csc) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;
        PermanentHasSubtypePredicate predicate = new PermanentHasSubtypePredicate(csc.subtype());
        return battlefield.stream()
                .anyMatch(p -> gameQueryService.matchesPermanentPredicate(gameData, p, predicate));
    }

    private boolean isControlsPermanentConditionMet(GameData gameData, StackEntry entry,
                                                    ControlsPermanentConditionalEffect cpc) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;
        return battlefield.stream()
                .anyMatch(p -> gameQueryService.matchesPermanentPredicate(gameData, p, cpc.filter()));
    }

    private boolean isNoOtherSubtypeConditionMet(GameData gameData, StackEntry entry,
                                                    NoOtherSubtypeConditionalEffect noOther) {
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return true;
        PermanentHasSubtypePredicate predicate = new PermanentHasSubtypePredicate(noOther.subtype());
        return battlefield.stream()
                .noneMatch(p -> !p.getId().equals(sourcePermanentId)
                        && gameQueryService.matchesPermanentPredicate(gameData, p, predicate));
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

    private boolean isActivationCountConditionMet(GameData gameData, StackEntry entry,
                                                  ActivationCountConditionalEffect acc) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) return false;
        var perAbilityCounts = gameData.activatedAbilityUsesThisTurn.get(sourcePermanentId);
        if (perAbilityCounts == null) return false;
        int count = perAbilityCounts.getOrDefault(acc.abilityIndex(), 0);
        return count >= acc.threshold();
    }

    private boolean isSourceDidntAttackThisTurn(GameData gameData, StackEntry entry) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) return true;
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) return false;
        return !source.isAttackedThisTurn();
    }

    private boolean isNoSpellsCastLastTurn(GameData gameData) {
        if (gameData.spellsCastLastTurn.isEmpty()) return true;
        return gameData.spellsCastLastTurn.values().stream().mapToInt(Integer::intValue).sum() == 0;
    }

    private boolean isTwoOrMoreSpellsCastLastTurn(GameData gameData) {
        return gameData.spellsCastLastTurn.values().stream().anyMatch(count -> count >= 2);
    }

    private boolean isAttackingAlone(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;
        long attackingCount = battlefield.stream().filter(Permanent::isAttacking).count();
        return attackingCount == 1;
    }
}
