package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CombatDamagePhase1State;
import com.github.laxika.magicalvibes.model.CombatDamageState;
import com.github.laxika.magicalvibes.model.CombatDamageTarget;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.SourceDamageRedirectShield;
import com.github.laxika.magicalvibes.model.SourceNextDamageToAnyTargetShield;
import com.github.laxika.magicalvibes.model.SourcePermanentAndControllerNextDamageRedirectShield;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageEffect;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageDraw;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageBecomeMonarch;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageLookAtHandAndDraw;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageLoot;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageReflection;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageToken;
import com.github.laxika.magicalvibes.model.action.DelayedDestroyCreatureDamagedByWatchedCreature;
import com.github.laxika.magicalvibes.model.action.DelayedDestroyCreatureDealingCombatDamageToPlaneswalker;
import com.github.laxika.magicalvibes.model.action.DelayedNamedCreatureCombatDamage;
import com.github.laxika.magicalvibes.model.action.DelayedWatchedCreaturesCombatDamage;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.condition.NoMonarch;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.condition.SourceHasChosenMode;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageAsThoughUnblockedEffect;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageToDefendingCreatureWhenUnblockedEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CombatDamageAmountAwareEffect;
import com.github.laxika.magicalvibes.model.effect.CombatDamageAssignmentToDefendingPlayerAndCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CombatDamageDealerAwareEffect;
import com.github.laxika.magicalvibes.model.effect.CombatDamageDealerReferencingEffect;
import com.github.laxika.magicalvibes.model.effect.CombatDamageResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.CombatDamageTriggerContextEffect;
import com.github.laxika.magicalvibes.model.effect.CombatExcessDamageAwareEffect;
import com.github.laxika.magicalvibes.model.effect.CombatOpponentReferencingEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DamageSourceAwareEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerAwareEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DefendingPlayerAssignsCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DamagedPlayerControlsTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.FirstestStrikeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToControlledCreatureCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToDamageDealtEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardCardChoosingEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;
import com.github.laxika.magicalvibes.model.effect.LoseGameIfSourceDealtDamageToPlayerThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PerDamageSourceTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceCombatDamageWithMillEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentsOnCombatDamageToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TakeInitiativeEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfAndAttachToCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.replacement.DamageEvent;
import com.github.laxika.magicalvibes.model.replacement.DamagePortion;
import com.github.laxika.magicalvibes.service.effect.replacement.damage.RedirectedDamageRules;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.combat.attack.CombatAttackService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.OncePerTurnTriggerSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerContext;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Resolves combat damage: first strike / double strike phases, damage distribution (trample,
 * deathtouch), manual damage assignment, infect, lifelink, damage redirection, creature death,
 * and all combat damage triggers (ON_COMBAT_DAMAGE_TO_PLAYER, ON_COMBAT_DAMAGE_TO_CREATURE,
 * ON_DEALT_DAMAGE, ON_DAMAGE_TO_PLAYER).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CombatDamageService {

    private enum DamagePhase {
        FIRSTEST_STRIKE,
        FIRST_STRIKE,
        REGULAR
    }

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;
    private final DamagePreventionService damagePreventionService;
    private final GraveyardService graveyardService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final TriggerCollectionService triggerCollectionService;
    private final com.github.laxika.magicalvibes.service.effect.GrantedTriggeredAbilitySupport grantedTriggeredAbilitySupport;
    private final LifeSupport lifeSupport;
    private final GraveyardTargetingService graveyardTargetingService;
    private final CombatAttackService combatAttackService;
    private final CombatTriggerService combatTriggerService;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport damageSupport;
    private final PermanentCounterSupport permanentCounterSupport;
    private final com.github.laxika.magicalvibes.service.state.StateBasedActionService stateBasedActionService;
    private final com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport battleDefeatSupport;

    /**
     * Resolves combat damage for the current combat phase.
     */
    public CombatResult resolveCombatDamage(GameData gameData) {
        if (gameData.pendingCombatDamageState != null
                && gameData.pendingCombatDamageState.awaitingOptionalDamageChoices) {
            if (gameData.pendingCombatDamageState.pendingOptionalDamageChoices > 0) return CombatResult.DONE;
            return completePendingCombatDamage(gameData);
        }
        UUID activeId = gameData.activePlayerId;
        UUID defenderId = gameQueryService.getOpponentId(gameData, activeId);

        List<Permanent> atkBf = gameData.playerBattlefields.get(activeId);
        List<Permanent> defBf = gameData.playerBattlefields.get(defenderId);

        // Re-entry: if pending damage assignments remain (for either damage step), send the next
        // assignment request. Attacker assignments are collected before blocker divisions (CR 510.1).
        if ((gameData.combatDamagePhase1Complete || gameData.combatDamageFirstStrikeAssignmentPhase)
                && (!gameData.combatDamagePendingIndices.isEmpty()
                        || !gameData.combatDamagePendingBlockerIndices.isEmpty())) {
            sendNextCombatDamageAssignment(gameData, atkBf, defBf, activeId, defenderId);
            return CombatResult.DONE;
        }

        List<Integer> attackingIndices = combatAttackService.getAttackingCreatureIndices(gameData, activeId);

        Map<Integer, List<Integer>> blockerMap = buildBlockerMap(atkBf, defBf, attackingIndices);

        // Check if any combat creature has firstest strike, first strike, or double strike.
        boolean anyFirstestStrike = gameQueryService.withQueryScope(gameData, () -> {
            for (int i : attackingIndices) {
                if (hasFirstestStrike(gameData, atkBf.get(i))) return true;
            }
            for (List<Integer> blockers : blockerMap.values()) {
                for (int i : blockers) {
                    if (hasFirstestStrike(gameData, defBf.get(i))) return true;
                }
            }
            return false;
        });

        boolean anyFirstStrike = gameQueryService.withQueryScope(gameData, () -> {
            for (int i : attackingIndices) {
                if (hasFirstOrDoubleStrike(gameData, atkBf.get(i))) return true;
            }
            for (List<Integer> blockers : blockerMap.values()) {
                for (int i : blockers) {
                    if (hasFirstOrDoubleStrike(gameData, defBf.get(i))) return true;
                }
            }
            return false;
        });

        CombatDamageState state = new CombatDamageState();

        // Restore regular-step state if re-entering after damage assignment
        if (gameData.combatDamagePhase1Complete) {
            restorePhase1State(gameData, state);
        }

        // Firstest-strike combat damage happens before first-strike combat damage, and may only
        // be dealt to creatures.
        if (!gameData.combatDamageFirstestStrikeStepComplete
                && !gameData.combatDamagePhase1Complete
                && anyFirstestStrike) {
            if (!gameData.combatDamageFirstestStrikeAssignmentPhase
                    && collectManualAssignments(gameData, state, blockerMap, atkBf, defBf,
                    DamagePhase.FIRSTEST_STRIKE)) {
                gameData.combatDamagePhase1State = savePhase1State(state, blockerMap, anyFirstStrike);
                gameData.combatDamageFirstestStrikeAssignmentPhase = true;
                sendNextCombatDamageAssignment(gameData, atkBf, defBf, activeId, defenderId);
                return CombatResult.DONE;
            }
            if (prepareSharedCombatPrevention(gameData, atkBf, defBf)) return CombatResult.DONE;
            resolveDamagePhase(gameData, state, blockerMap, atkBf, defBf, activeId, defenderId,
                    DamagePhase.FIRSTEST_STRIKE);
            if (parkOptionalDamageChoices(gameData, state, DamagePhase.FIRSTEST_STRIKE)) {
                return CombatResult.DONE;
            }
            if (shouldPutCombatDamageOnStack(gameData)) {
                return putCombatDamageOnStack(gameData, state, DamagePhase.FIRSTEST_STRIKE);
            }
            CombatResult result = finishCombatDamageStep(gameData, state, atkBf, defBf,
                    activeId, defenderId);
            gameData.combatDamageFirstestStrikeAssignmentPhase = false;
            gameData.combatDamagePlayerAssignments.clear();
            gameData.combatDamageBlockerAssignments.clear();
            gameData.combatDamagePhase1State = null;
            gameData.combatDamageFirstestStrikeStepComplete = true;
            return result == CombatResult.ADVANCE_AND_AUTO_PASS ? CombatResult.AUTO_PASS_ONLY : result;
        }

        // First-strike combat damage step. Its triggers resolve before regular combat damage.
        if (!gameData.combatDamageFirstStrikeStepComplete
                && !gameData.combatDamagePhase1Complete
                && anyFirstStrike) {
            // CR 510.1c-d: damage assignment choices exist in this step too. Collect them before
            // dealing first-strike damage; the collected assignments are consumed by this step
            // only — a double striker assigns its regular-step damage anew.
            if (!gameData.combatDamageFirstStrikeAssignmentPhase
                    && collectManualAssignments(gameData, state, blockerMap, atkBf, defBf,
                    DamagePhase.FIRST_STRIKE)) {
                gameData.combatDamagePhase1State = savePhase1State(state, blockerMap, anyFirstStrike);
                gameData.combatDamageFirstStrikeAssignmentPhase = true;
                sendNextCombatDamageAssignment(gameData, atkBf, defBf, activeId, defenderId);
                return CombatResult.DONE;
            }
            if (prepareSharedCombatPrevention(gameData, atkBf, defBf)) return CombatResult.DONE;
            resolveDamagePhase(gameData, state, blockerMap, atkBf, defBf, activeId, defenderId,
                    DamagePhase.FIRST_STRIKE);
            if (parkOptionalDamageChoices(gameData, state, DamagePhase.FIRST_STRIKE)) {
                return CombatResult.DONE;
            }
            if (shouldPutCombatDamageOnStack(gameData)) {
                return putCombatDamageOnStack(gameData, state, DamagePhase.FIRST_STRIKE);
            }
            CombatResult result = finishCombatDamageStep(gameData, state, atkBf, defBf,
                    activeId, defenderId);
            gameData.combatDamageFirstStrikeAssignmentPhase = false;
            gameData.combatDamagePlayerAssignments.clear();
            gameData.combatDamageBlockerAssignments.clear();
            gameData.combatDamagePhase1State = null;
            gameData.combatDamageFirstStrikeStepComplete = true;
            return result == CombatResult.ADVANCE_AND_AUTO_PASS ? CombatResult.AUTO_PASS_ONLY : result;
        }

        // Check if any regular-step attackers or multi-blocking creatures need manual damage assignment
        if (!gameData.combatDamagePhase1Complete
                && collectManualAssignments(gameData, state, blockerMap, atkBf, defBf,
                DamagePhase.REGULAR)) {
            gameData.combatDamagePhase1State = savePhase1State(state, blockerMap, anyFirstStrike);
            gameData.combatDamagePhase1Complete = true;
            sendNextCombatDamageAssignment(gameData, atkBf, defBf, activeId, defenderId);
            return CombatResult.DONE;
        }

        if (prepareSharedCombatPrevention(gameData, atkBf, defBf)) return CombatResult.DONE;
        resolveDamagePhase(gameData, state, blockerMap, atkBf, defBf, activeId, defenderId,
                DamagePhase.REGULAR);

        if (parkOptionalDamageChoices(gameData, state, DamagePhase.REGULAR)) {
            return CombatResult.DONE;
        }
        if (shouldPutCombatDamageOnStack(gameData)) {
            return putCombatDamageOnStack(gameData, state, DamagePhase.REGULAR);
        }

        gameData.combatDamageFirstestStrikeStepComplete = false;
        gameData.combatDamageFirstStrikeStepComplete = false;
        return finishCombatDamageStep(gameData, state, atkBf, defBf, activeId, defenderId);
    }

    private boolean prepareSharedCombatPrevention(GameData gameData, List<Permanent> attackers,
                                                  List<Permanent> defenders) {
        for (var assignment : gameData.combatDamagePlayerAssignments.entrySet()) {
            if (assignment.getKey() < attackers.size()
                    && prepareSharedCombatPreventionForSource(
                            gameData, attackers.get(assignment.getKey()), assignment.getValue())) return true;
        }
        for (var assignment : gameData.combatDamageBlockerAssignments.entrySet()) {
            if (assignment.getKey() < defenders.size()
                    && prepareSharedCombatPreventionForSource(
                            gameData, defenders.get(assignment.getKey()), assignment.getValue())) return true;
        }
        return false;
    }

    private boolean prepareSharedCombatPreventionForSource(GameData gameData, Permanent damageSource,
                                                           Map<UUID, Integer> assignedDamage) {
        if (!gameQueryService.isDamagePreventable(gameData, true)
                || gameQueryService.damageCantBePreventedFromSource(gameData, damageSource, true)) return false;
        for (UUID controllerId : gameData.orderedPlayerIds) {
            for (Permanent preventionSource : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
                for (CardEffect effect : gameQueryService.getActiveStaticEffects(gameData, preventionSource)) {
                    if (!(effect instanceof com.github.laxika.magicalvibes.model.effect.PreventXDamagePerSourceToControllerAndCreaturesEffect prevention)) continue;
                    if (prevention.creatureSourcesOnly() && !gameQueryService.isCreature(gameData, damageSource)) continue;
                    var preventionSources = gameData.combatSharedDamagePreventionAllocations
                            .computeIfAbsent(damageSource.getId(), ignored -> new LinkedHashMap<>());
                    if (preventionSources.containsKey(preventionSource.getId())) continue;
                    Map<UUID, Integer> eligible = new LinkedHashMap<>();
                    assignedDamage.forEach((recipientId, amount) -> {
                        Permanent recipient = gameQueryService.findPermanentById(gameData, recipientId);
                        if (amount > 0 && (recipientId.equals(controllerId)
                                || recipient != null && gameQueryService.isCreature(gameData, recipient)
                                && controllerId.equals(gameQueryService.findPermanentController(gameData, recipientId))
                                && !recipient.isDamageCantBePreventedOrRedirectedThisTurn())) {
                            eligible.put(recipientId, amount);
                        }
                    });
                    int total = eligible.values().stream().mapToInt(Integer::intValue).sum();
                    int amount = Math.min(total, Math.max(0, amountEvaluationService.evaluate(gameData,
                            prevention.amount(), AmountContext.forStaticEffect(preventionSource, controllerId))));
                    if (eligible.size() < 2 || amount <= 0) continue;
                    Map<UUID, Integer> allocations = new LinkedHashMap<>();
                    preventionSources.put(preventionSource.getId(), allocations);
                    if (amount >= total) {
                        allocations.putAll(eligible);
                    } else {
                        beginSharedCombatPreventionChoice(gameData,
                                new com.github.laxika.magicalvibes.model.ChoiceContext.CombatSharedDamagePreventionChoice(
                                        preventionSource.getId(), damageSource.getId(), controllerId, amount,
                                        new ArrayList<>(eligible.keySet()), eligible));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void beginSharedCombatPreventionChoice(GameData gameData,
            com.github.laxika.magicalvibes.model.ChoiceContext.CombatSharedDamagePreventionChoice choice) {
        Map<UUID, Integer> allocations = gameData.combatSharedDamagePreventionAllocations
                .get(choice.damageSourceId()).get(choice.preventionSourceId());
        List<String> options = choice.recipients().stream()
                .filter(id -> allocations.getOrDefault(id, 0) < choice.damage().get(id))
                .map(id -> sharedPreventionRecipientLabel(gameData, choice, id)).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                choice.controllerId(), null, null, choice, options,
                "Choose a recipient to prevent 1 damage to (" + choice.remaining() + " remaining)."));
    }

    private String sharedPreventionRecipientLabel(GameData gameData,
            com.github.laxika.magicalvibes.model.ChoiceContext.CombatSharedDamagePreventionChoice choice,
            UUID recipientId) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, recipientId);
        String name = permanent == null ? gameData.playerIdToName.get(recipientId)
                : permanent.getCard().getName();
        return name + " (" + (choice.recipients().indexOf(recipientId) + 1) + ")";
    }

    /** Records one point of a shared prevention effect before combat damage is dealt. */
    public boolean completeSharedCombatPreventionChoice(GameData gameData,
            com.github.laxika.magicalvibes.model.ChoiceContext.CombatSharedDamagePreventionChoice choice,
            String label) {
        UUID recipientId = choice.recipients().stream()
                .filter(id -> sharedPreventionRecipientLabel(gameData, choice, id).equals(label))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Invalid prevention recipient"));
        Map<UUID, Integer> allocations = gameData.combatSharedDamagePreventionAllocations
                .get(choice.damageSourceId()).get(choice.preventionSourceId());
        if (!choice.recipients().contains(recipientId)
                || allocations.getOrDefault(recipientId, 0) >= choice.damage().get(recipientId)) {
            throw new IllegalArgumentException("Invalid prevention recipient");
        }
        allocations.merge(recipientId, 1, Integer::sum);
        if (choice.remaining() > 1) {
            beginSharedCombatPreventionChoice(gameData,
                    new com.github.laxika.magicalvibes.model.ChoiceContext.CombatSharedDamagePreventionChoice(
                            choice.preventionSourceId(), choice.damageSourceId(), choice.controllerId(),
                            choice.remaining() - 1, choice.recipients(), choice.damage()));
            return false;
        }
        return true;
    }

    private boolean parkOptionalDamageChoices(GameData gameData, CombatDamageState state,
                                                DamagePhase phase) {
        if (state.pendingOptionalDamageChoices == 0) return false;
        state.awaitingOptionalDamageChoices = true;
        gameData.pendingCombatDamageState = state;
        gameData.pendingCombatDamageFirstestStrikePhase = phase == DamagePhase.FIRSTEST_STRIKE;
        gameData.pendingCombatDamageFirstStrikePhase = phase == DamagePhase.FIRST_STRIKE;
        playerInputService.processNextMayAbility(gameData);
        return true;
    }

    private boolean shouldPutCombatDamageOnStack(GameData gameData) {
        return gameData.pendingCombatDamageState == null
                && gameQueryService.combatDamageUsesStack(gameData);
    }

    private CombatResult putCombatDamageOnStack(GameData gameData, CombatDamageState state,
                                                 DamagePhase phase) {
        gameData.pendingCombatDamageState = state;
        gameData.pendingCombatDamageFirstestStrikePhase = phase == DamagePhase.FIRSTEST_STRIKE;
        gameData.pendingCombatDamageFirstStrikePhase = phase == DamagePhase.FIRST_STRIKE;

        Card sourceCard = null;
        UUID sourcePermanentId = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (gameQueryService.hasActiveStaticEffect(
                        gameData, permanent,
                        com.github.laxika.magicalvibes.model.effect.CombatDamageUsesStackEffect.class)) {
                    sourceCard = permanent.getCard();
                    sourcePermanentId = permanent.getId();
                    break;
                }
            }
            if (sourceCard != null) break;
        }

        StackEntry damageEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                gameData.activePlayerId,
                "Combat damage",
                new ArrayList<>(List.of(new CombatDamageResolutionEffect())),
                null,
                sourcePermanentId);
        damageEntry.setNonTargeting(true);
        gameData.stack.add(damageEntry);
        gameLogService.append(gameData, GameLog.text("Combat damage is put on the stack."));
        return CombatResult.AUTO_PASS_ONLY;
    }

    /** Resolves the pending combat-damage stack object after both players pass priority. */
    public void resolvePendingCombatDamage(GameData gameData) {
        completePendingCombatDamage(gameData);
    }

    private CombatResult completePendingCombatDamage(GameData gameData) {
        CombatDamageState state = gameData.pendingCombatDamageState;
        if (state == null) return CombatResult.DONE;

        DamagePhase phase = gameData.pendingCombatDamageFirstestStrikePhase
                ? DamagePhase.FIRSTEST_STRIKE
                : gameData.pendingCombatDamageFirstStrikePhase
                ? DamagePhase.FIRST_STRIKE
                : DamagePhase.REGULAR;
        gameData.pendingCombatDamageState = null;
        gameData.pendingCombatDamageFirstestStrikePhase = false;
        gameData.pendingCombatDamageFirstStrikePhase = false;

        UUID activeId = gameData.activePlayerId;
        UUID defenderId = gameQueryService.getOpponentId(gameData, activeId);
        List<Permanent> atkBf = gameData.playerBattlefields.getOrDefault(activeId, List.of());
        List<Permanent> defBf = gameData.playerBattlefields.getOrDefault(defenderId, List.of());
        CombatResult result = finishCombatDamageStep(gameData, state, atkBf, defBf, activeId, defenderId);

        if (phase == DamagePhase.FIRSTEST_STRIKE) {
            gameData.combatDamageFirstestStrikeAssignmentPhase = false;
            gameData.combatDamagePlayerAssignments.clear();
            gameData.combatDamageBlockerAssignments.clear();
            gameData.combatDamagePhase1State = null;
            gameData.combatDamageFirstestStrikeStepComplete = true;
        } else if (phase == DamagePhase.FIRST_STRIKE) {
            gameData.combatDamageFirstStrikeAssignmentPhase = false;
            gameData.combatDamagePlayerAssignments.clear();
            gameData.combatDamageBlockerAssignments.clear();
            gameData.combatDamagePhase1State = null;
            gameData.combatDamageFirstStrikeStepComplete = true;
        } else {
            gameData.combatDamageFirstestStrikeStepComplete = false;
            gameData.combatDamageFirstStrikeStepComplete = false;
        }
        return phase != DamagePhase.REGULAR && result == CombatResult.ADVANCE_AND_AUTO_PASS
                ? CombatResult.AUTO_PASS_ONLY : result;
    }

    private CombatResult finishCombatDamageStep(GameData gameData, CombatDamageState state,
                                                 List<Permanent> atkBf, List<Permanent> defBf,
                                                 UUID activeId, UUID defenderId) {
        // CR 510.1 — Snapshot Phyrexian Unlife infect conversion before lifelink changes life totals.
        // All combat damage is simultaneous, so the infect check must use pre-damage life.
        state.defenderDamageAsInfect = gameQueryService.shouldDamageBeDealtAsInfect(gameData, defenderId);
        Set<Permanent> lifelinkDealers = Collections.newSetFromMap(new IdentityHashMap<>());
        for (List<Permanent> side : List.of(atkBf, defBf)) {
            for (Permanent dealer : side) {
                if (gameQueryService.hasKeyword(gameData, dealer, Keyword.LIFELINK)) {
                    lifelinkDealers.add(dealer);
                }
            }
        }

        applyDamageToEachPlayer(gameData, state, defenderId);
        Map<UUID, Set<Permanent>> damagedPlayerSources = new LinkedHashMap<>();
        state.redirectedCombatDamageSourcesToPlayers.forEach((playerId, sources) ->
                damagedPlayerSources.put(playerId, new LinkedHashSet<>(sources)));
        state.combatDamageDealtToPlayer.forEach((source, damage) -> {
            if (damage > 0) damagedPlayerSources.computeIfAbsent(
                    state.combatDamagePlayerRecipients.getOrDefault(source, defenderId),
                    ignored -> new LinkedHashSet<>()).add(source);
        });
        damagedPlayerSources.forEach((playerId, sources) -> {
            Map<UUID, Integer> toxicByController = new LinkedHashMap<>();
            int infectDamage = state.infectDamageToPlayers.getOrDefault(playerId, 0);
            if (infectDamage > 0) {
                toxicByController.put(activeId, infectDamage);
            }
            for (Permanent source : sources) {
                if (!gameQueryService.hasKeyword(gameData, source, Keyword.TOXIC)) continue;
                List<CardEffect> toxicEffects = new ArrayList<>(gameQueryService.getActiveStaticEffects(gameData, source));
                toxicEffects.addAll(gameQueryService.getGrantedEffects(gameData, source));
                int toxic = toxicEffects.stream()
                        .filter(ToxicEffect.class::isInstance).map(ToxicEffect.class::cast)
                        .mapToInt(ToxicEffect::value).sum();
                toxic += gameData.perpetualToxicValues.getOrDefault(source.getOriginalCard().getId(), 0);
                UUID controllerId = state.combatDamageDealerControllers.getOrDefault(source,
                        gameQueryService.findPermanentController(gameData, source.getId()));
                if (toxic > 0 && controllerId != null) toxicByController.merge(controllerId, toxic, Integer::sum);
            }
            toxicByController.forEach((controllerId, amount) -> lifeSupport.applyPoisonCounters(
                    gameData, playerId, amount, "Combat damage", controllerId));
        });
        updateMonarchFromCombatDamage(gameData, state, defenderId);
        state.combatDamageDealtToPlayer.forEach((source, damage) -> {
            if (damage > 0 && gameData.isCommander(source.getOriginalCard().getId())) {
                gameData.commanderDamageReceived.computeIfAbsent(
                        state.combatDamagePlayerRecipients.getOrDefault(source, defenderId),
                        id -> new java.util.HashMap<>())
                        .merge(source.getOriginalCard().getId(), damage, Integer::sum);
            }
        });

        // All combat damage of this step is applied as one simultaneous recording (CR 510.4):
        // marked damage + deathtouch flags on creatures, life loss on the player, loyalty on
        // planeswalkers. Nothing dies here — the state-based action check below is the single
        // place combat casualties are determined (CR 704.5f/5g/5h/5i).
        int stackSizeBeforeDamageEvents = gameData.stack.size();
        updateMarkedDamageFromCombat(gameData, atkBf, defBf, state);
        List<DealtDamageTriggerData> dealtDamageTriggerData = collectDealtDamageTriggerData(gameData, state);
        gameData.combatSharedDamagePreventionAllocations.clear();
        applyPendingDralnuReplacements(gameData, state);
        applyPlaneswalkerDamage(gameData, state);
        // CR 510.2 — lifelink gains life as the simultaneous damage is dealt, so it can't change
        // whether a life-dependent prevention applied to that same damage. Before dead creatures leave.
        processLifelink(gameData, state.combatDamageDealt, lifelinkDealers);
        processGainLifeEqualToDamageDealt(gameData, state.combatDamageDealt);
        processGainLifeEqualToControlledCreatureCombatDamage(gameData, state.combatDamageDealt);
        checkCombatExcessDamageTriggers(gameData, state, atkBf, defBf);
        processAllyDealtDamageToPlaneswalkerTriggers(gameData, state);
        processOpponentSourceDamageToYouOrYourPermanentTriggers(gameData, state);
        checkGraveyardCombatDamageToYouOrPlaneswalkerTriggers(gameData, state, defenderId);

        for (var entry : state.combatDamageDealt.entrySet()) {
            if (entry.getValue() > 0) {
                triggerCollectionService.collectEnchantedCreatureDealsDamageTriggers(
                        gameData, entry.getKey(), entry.getValue(), state.enchantedCreatureDealsDamageTriggers);
            }
        }

        for (var sourceEntry : state.combatDamageAmountsToCreatures.entrySet()) {
            for (var targetEntry : sourceEntry.getValue().entrySet()) {
                triggerCollectionService.collectEnchantedCreatureDealsDamageToCreatureTriggers(
                        gameData, sourceEntry.getKey(), targetEntry.getKey(), targetEntry.getValue(),
                        state.enchantedCreatureDealsDamageTriggers);
            }
        }

        for (Permanent source : state.combatDamageDealt.keySet()) {
            List<CardEffect> effects = new ArrayList<>(
                    source.getTemporaryTriggeredEffects(EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE));
            effects.addAll(source.getPersistentTriggeredEffects(EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, source, EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE));
            state.selfDealsCombatDamageEffects.put(source, effects);

            List<CardEffect> playerOrPlaneswalkerEffects = new ArrayList<>(
                    source.getTemporaryTriggeredEffects(EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER));
            playerOrPlaneswalkerEffects.addAll(source.getPersistentTriggeredEffects(
                    EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER));
            playerOrPlaneswalkerEffects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, source, EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER));
            state.selfDealsCombatDamageToPlayerOrPlaneswalkerEffects.put(source, playerOrPlaneswalkerEffects);

            List<CardEffect> playerOrBattleEffects = new ArrayList<>(
                    source.getTemporaryTriggeredEffects(EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE));
            playerOrBattleEffects.addAll(source.getPersistentTriggeredEffects(
                    EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE));
            playerOrBattleEffects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, source, EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE));
            state.selfDealsCombatDamageToPlayerOrBattleEffects.put(source, playerOrBattleEffects);
        }

        // Snapshot attacker IDs so blocking state can be cleaned up for attackers that die.
        Set<UUID> attackerIdsBefore = new HashSet<>();
        for (Permanent p : atkBf) {
            attackerIdsBefore.add(p.getId());
        }
        Map<UUID, String> playerPrefixBefore = new LinkedHashMap<>();
        Map<UUID, Card> cardBefore = new LinkedHashMap<>();
        for (Permanent p : atkBf) {
            playerPrefixBefore.put(p.getId(), gameData.playerIdToName.get(activeId) + "'s ");
            cardBefore.put(p.getId(), p.getCard());
        }
        for (Permanent p : defBf) {
            playerPrefixBefore.put(p.getId(), gameData.playerIdToName.get(defenderId) + "'s ");
            cardBefore.put(p.getId(), p.getCard());
        }

        Map<Permanent, List<CardEffect>> damageToCreatureEffects = new HashMap<>();
        for (Permanent source : state.combatDamageDealtToCreatures.keySet()) {
            List<CardEffect> effects = new ArrayList<>(
                    source.getCard().getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE));
            effects.addAll(source.getTemporaryTriggeredEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE));
            effects.addAll(source.getPersistentTriggeredEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, source, EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE));
            damageToCreatureEffects.put(source, effects);
        }
        Map<Permanent, UUID> attachedDamageSources = new LinkedHashMap<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (permanent.isAttached()) {
                attachedDamageSources.put(new Permanent(permanent), controllerId);
            }
        });
        Map<UUID, Permanent> damagedCreatureSnapshots = snapshotCombatDamagedCreatures(gameData, state);
        int beforeGlobalDamageTriggers = gameData.stack.size();
        processAnyCreatureDealtDamageTriggers(gameData, state, damagedCreatureSnapshots);
        List<StackEntry> globalDamageTriggers = new ArrayList<>(
                gameData.stack.subList(beforeGlobalDamageTriggers, gameData.stack.size()));
        gameData.stack.subList(beforeGlobalDamageTriggers, gameData.stack.size()).clear();
        snapshotDelayedCombatDamageDrawSources(gameData, state);
        snapshotDelayedCombatDamageLookAtHandAndDrawSources(gameData, state, defenderId);
        snapshotSelfDealsDamageEffects(gameData, state);
        Map<UUID, List<TriggerCollectionService.AllyDamageTriggerSnapshot>> allyDamageWatchers =
                triggerCollectionService.snapshotAllyCreaturesDealDamageToOpponentTriggers(gameData);
        // "Whenever a [creature] deals damage" abilities trigger as the damage is dealt, so a watcher
        // that is destroyed by that same damage (a Sliver watching itself) must be scanned before
        // state-based actions remove it. The triggers are put on the stack after the other combat
        // damage triggers, as they were when this ran after the state-based actions.
        int beforeSourceDamageReflections = gameData.stack.size();
        processSourceDealsDamageReflectionTriggers(gameData, state, defenderId, damagedCreatureSnapshots);
        List<StackEntry> sourceDamageReflectionTriggers = new ArrayList<>(
                gameData.stack.subList(beforeSourceDamageReflections, gameData.stack.size()));
        gameData.stack.subList(beforeSourceDamageReflections, gameData.stack.size()).clear();
        int beforeSelfAllyDamageReflections = gameData.stack.size();
        processSelfDealtDamageToCreatureReflectionTriggers(gameData, state, damagedCreatureSnapshots);
        List<StackEntry> selfAllyDamageReflectionTriggers = new ArrayList<>(
                gameData.stack.subList(beforeSelfAllyDamageReflections, gameData.stack.size()));
        gameData.stack.subList(beforeSelfAllyDamageReflections, gameData.stack.size()).clear();
        int beforeWatcherAllyDamageReflections = gameData.stack.size();
        processWatchersDealtDamageToCreatureReflectionTriggers(gameData, state, damagedCreatureSnapshots);
        List<StackEntry> watcherAllyDamageReflectionTriggers = new ArrayList<>(
                gameData.stack.subList(beforeWatcherAllyDamageReflections, gameData.stack.size()));
        gameData.stack.subList(beforeWatcherAllyDamageReflections, gameData.stack.size()).clear();
        boolean damageEventTriggersQueued = gameData.stack.size() > stackSizeBeforeDamageEvents;
        Map<UUID, List<Permanent>> battlefieldsBeforeStateBasedActions = new LinkedHashMap<>();
        gameData.playerBattlefields.forEach((playerId, battlefield) ->
                battlefieldsBeforeStateBasedActions.put(playerId, new ArrayList<>(battlefield)));
        stateBasedActionService.performStateBasedActions(gameData);

        if (gameData.status == com.github.laxika.magicalvibes.model.GameStatus.FINISHED) {
            return CombatResult.DONE;
        }

        // Diff the battlefields to report casualties and clear orphaned blocking state.
        Set<UUID> survivingIds = new HashSet<>();
        gameData.forEachPermanent((pid, p) -> survivingIds.add(p.getId()));
        List<UUID> deadCreatureIds = new ArrayList<>();
        Set<UUID> deadAttackerIds = new HashSet<>();
        for (UUID id : cardBefore.keySet()) {
            if (!survivingIds.contains(id)) {
                deadCreatureIds.add(id);
                if (attackerIdsBefore.contains(id)) {
                    deadAttackerIds.add(id);
                }
            }
        }
        clearOrphanedBlockingState(defBf, deadAttackerIds);
        Map<UUID, List<Permanent>> departedWatchersByController = new LinkedHashMap<>();
        battlefieldsBeforeStateBasedActions.forEach((playerId, permanents) -> {
            for (Permanent permanent : permanents) {
                if (!survivingIds.contains(permanent.getId())) {
                    departedWatchersByController.computeIfAbsent(playerId, ignored -> new ArrayList<>())
                            .add(permanent);
                }
            }
        });

        if (!deadCreatureIds.isEmpty()) {
            GameLog.Builder logBuilder = GameLog.builder();
            for (int i = 0; i < deadCreatureIds.size(); i++) {
                UUID id = deadCreatureIds.get(i);
                if (i > 0) {
                    logBuilder.text(", ");
                }
                logBuilder.text(playerPrefixBefore.get(id)).card(cardBefore.get(id));
            }
            logBuilder.text(" died in combat.");
            gameLogService.append(gameData, logBuilder.build());
        }

        log.info("Game {} - Combat damage resolved: {} damage to defender, {} creatures died",
                gameData.id, state.damageToDefendingPlayer, deadCreatureIds.size());

        int stackSizeBeforeDamageTriggers = gameData.stack.size();
        gameData.stack.addAll(globalDamageTriggers);

        // Death triggers were already collected by the SBA pass above. They must remain on the
        // stack for normal priority rather than being folded into the engine's auto-resolved
        // combat-damage trigger batch below.
        updateMonarchFromCombatDamage(gameData, state, defenderId);
        queueInitiativeTransferFromCombatDamage(gameData, state, defenderId);
        gameData.stack.addAll(state.allyCreatureDealsDamageToPlaneswalkerTriggers);
        gameData.stack.addAll(state.enchantedCreatureDealsDamageTriggers);
        processSelfDealsCombatDamageTriggers(gameData, state, defenderId);
        processSelfDealsCombatDamageToPlayerOrPlaneswalkerTriggers(gameData, state);
        processSelfDealsCombatDamageToPlayerOrBattleTriggers(gameData, state);
        processCombatDamageToBattleTriggers(gameData, state);
        processCombatDamageToCreatureTriggers(gameData, state.combatDamageDealtToCreatures,
                state.combatDamageDealerControllers, state.combatDamageTargetControllers, damageToCreatureEffects,
                state, damagedCreatureSnapshots);
        processCombatDamageToBlockingCreatureTriggers(gameData, state);

        // Acidic Dagger's delayed "destroy the non-Wall creature that creature damaged" trigger.
        processDelayedDestroyCreatureDamagedTriggers(gameData, state.combatDamageDealtToCreatures);

        processDelayedPlaneswalkerCombatDamageTriggers(gameData, state.combatDamageDealt);

        // Tamiyo, Field Researcher's +1: "until your next turn, whenever either of those creatures
        // deals combat damage, you draw a card".
        processDelayedWatchedCreatureCombatDamageTriggers(gameData, state);
        processDelayedNamedCreatureCombatDamageTriggers(gameData, state);

        // Process ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE reflection triggers (e.g. Greatbow Doyen)
        gameData.stack.addAll(watcherAllyDamageReflectionTriggers);
        processAllyDealtDamageToCreatureReflectionTriggers(gameData, state, damagedCreatureSnapshots);
        gameData.stack.addAll(selfAllyDamageReflectionTriggers);

        // Process ON_DEALT_DAMAGE triggers (e.g. Nested Ghoul)
        processDealtDamageTriggers(gameData, dealtDamageTriggerData);

        // Process combat-damage-received triggers (e.g. Wall of Essence)
        processCombatDamageReceivedTriggers(gameData, dealtDamageTriggerData);



        // Process ON_OPPONENT_CREATURE_DEALT_DAMAGE triggers (e.g. Kazarov)
        for (var entry : state.defDamageTaken.entrySet()) {
            if (entry.getValue() > 0) {
                triggerCollectionService.checkOpponentCreatureDealtDamageTriggers(gameData, defenderId);
            }
        }
        for (var entry : state.atkDamageTaken.entrySet()) {
            if (entry.getValue() > 0) {
                triggerCollectionService.checkOpponentCreatureDealtDamageTriggers(gameData, activeId);
            }
        }

        // A blocker can also deal combat damage to the defending player (Butcher Orgg).
        // Attribute triggers and damage history to each source's controller.
        Map<UUID, Map<UUID, Map<Permanent, Integer>>> playerDamageByRecipientAndController = new LinkedHashMap<>();
        state.combatDamageDealtToPlayer.forEach((source, damage) ->
                playerDamageByRecipientAndController.computeIfAbsent(
                        state.combatDamagePlayerRecipients.getOrDefault(source, defenderId),
                        ignored -> new LinkedHashMap<>()).computeIfAbsent(
                        state.combatDamageDealerControllers.getOrDefault(source, activeId),
                        ignored -> new LinkedHashMap<>()).put(source, damage));
        playerDamageByRecipientAndController.forEach((recipientId, damageByController) ->
                damageByController.forEach((controllerId, damageBySource) -> {
                    processCombatDamageToPlayerTriggers(gameData, damageBySource, controllerId, recipientId,
                            attachedDamageSources, allyDamageWatchers.getOrDefault(controllerId, List.of()),
                            departedWatchersByController);
                    triggerCollectionService.checkEmblemCombatDamageTriggers(
                            gameData, controllerId, recipientId, damageBySource);
                    processDelayedCombatDamageLootTriggers(gameData, damageBySource, controllerId);
                    processDelayedCombatDamageTokenTriggers(gameData, damageBySource, controllerId, recipientId);
                }));
        processDelayedCombatDamageBecomeMonarchTriggers(gameData, state, activeId);

        processDelayedCombatDamageLookAtHandAndDrawTriggers(gameData, state);

        processDelayedCombatDamageEffects(gameData, state);

        // Process delayed combat damage draw triggers (e.g. Flitterwing Nuisance's ability)
        processDelayedCombatDamageDrawTriggers(gameData, state);

        Map<UUID, Set<String>> firedBatchedTriggerKeysByRecipient = new HashMap<>();
        playerDamageByRecipientAndController.forEach((recipientId, damageByController) -> {
            Map<Permanent, Integer> damageBySource = new LinkedHashMap<>();
            damageByController.values().forEach(damageBySource::putAll);
            processCombatDamageReflectionTriggers(gameData, damageBySource, activeId, recipientId);
            Set<String> firedBatchedTriggerKeys = firedBatchedTriggerKeysByRecipient.computeIfAbsent(
                    recipientId, ignored -> new HashSet<>());
            damageBySource.forEach((source, amount) -> {
                if (amount <= 0) return;
                triggerCollectionService.checkDamageDealtToControllerTriggers(
                        gameData, recipientId, source.getId(), true, firedBatchedTriggerKeys);
                triggerCollectionService.checkEnchantedCreatureDealtDamageToControllerReflectTriggers(
                        gameData, recipientId, source.getId(), amount);
                UUID sourceController = state.combatDamageDealerControllers.getOrDefault(source, activeId);
                triggerCollectionService.checkAllySourceDealtDamageToOpponentTriggers(
                        gameData, recipientId, sourceController, source.getId(), amount);
                triggerCollectionService.checkCreatureDamageToYouOrYourPermanentTriggers(
                        gameData, recipientId, null, source, amount);
            });
            int totalDamage = damageBySource.values().stream().mapToInt(Integer::intValue).sum();
            triggerCollectionService.checkControllerDealtDamageTriggers(
                    gameData, recipientId, null, totalDamage);
            damageByController.forEach((controllerId, sources) -> {
                if (controllerId.equals(recipientId)) {
                    triggerCollectionService.checkControllerDealtDamageTriggers(
                            gameData, recipientId, controllerId,
                            sources.values().stream().mapToInt(Integer::intValue).sum(), false);
                    return;
                }
                sources.values().forEach(amount ->
                        triggerCollectionService.checkControllerDealtDamageTriggers(
                                gameData, recipientId, controllerId, amount, false));
            });
        });
        state.redirectedCombatDamageSourcesToPlayers.forEach((recipientId, sources) -> {
            Set<String> firedBatchedTriggerKeys = firedBatchedTriggerKeysByRecipient.computeIfAbsent(
                    recipientId, ignored -> new HashSet<>());
            for (Permanent source : sources) {
                boolean alreadyDispatched = state.combatDamageDealtToPlayer.getOrDefault(source, 0) > 0
                        && recipientId.equals(state.combatDamagePlayerRecipients.getOrDefault(source, defenderId));
                if (!alreadyDispatched) {
                    triggerCollectionService.checkDamageDealtToControllerTriggers(
                            gameData, recipientId, source.getId(), true, firedBatchedTriggerKeys);
                }
            }
        });

        recordCombatDamageBySource(gameData, state);

        // ON_ANY_SOURCE_DEALS_DAMAGE reflection triggers (e.g. Justice) were collected before the
        // state-based actions; combatDamageDealt is already summed per source for this step, matching
        // the "add up all the damage" ruling.
        gameData.stack.addAll(sourceDamageReflectionTriggers);

        combatTriggerService.reorderTriggersAPNAP(gameData, stackSizeBeforeDamageTriggers, activeId);

        // Graveyard combat-damage triggers pick their target while combat is still in the damage
        // step (CR 603.3d) — waiting for the next priority pass would let end of combat clear the
        // attacking flags out from under an "attacking creature" target restriction.
        if (!gameData.interaction.isAwaitingInput()
                && gameData.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)) {
            triggerCollectionService.processNextAttackTriggerTarget(gameData);
        }

        if (!gameData.interaction.isAwaitingInput()
                && gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class)) {
            triggerCollectionService.processNextETBTokenMultiTargetTrigger(gameData);
        }

        // "Whenever this creature is dealt damage" triggers with a target choose it as they are put on
        // the stack (CR 603.3d), still in the damage step rather than after the step has advanced.
        if (!gameData.interaction.isAwaitingInput()
                && gameData.hasPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class)) {
            triggerCollectionService.processNextSpellTargetTrigger(gameData);
        }

        if (gameData.interaction.isAwaitingInput()) {
            return CombatResult.DONE;
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return CombatResult.DONE;
        }

        if (damageEventTriggersQueued || gameData.stack.size() > stackSizeBeforeDamageTriggers) {
            return CombatResult.AUTO_PASS_RESOLVE_COMBAT_TRIGGERS;
        }

        return CombatResult.ADVANCE_AND_AUTO_PASS;
    }

    private void updateMonarchFromCombatDamage(GameData gameData, CombatDamageState state,
                                                UUID defenderId) {
        if (gameData.monarchPlayerId == null
                || (state.damageToDefendingPlayer <= 0 && state.poisonDamageToDefendingPlayer <= 0)) {
            return;
        }
        for (var damageEntry : state.combatDamageDealtToPlayer.entrySet()) {
            if (damageEntry.getValue() <= 0
                    || !gameData.monarchPlayerId.equals(state.combatDamagePlayerRecipients
                    .getOrDefault(damageEntry.getKey(), defenderId))) {
                continue;
            }
            UUID controllerId = state.combatDamageDealerControllers.get(damageEntry.getKey());
            if (controllerId == null) {
                controllerId = gameData.findControllerOf(damageEntry.getKey());
            }
            if (controllerId != null && !controllerId.equals(gameData.monarchPlayerId)) {
                StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY, null,
                        gameData.monarchPlayerId, "Combat damage causes a player to become the monarch",
                        List.of(new com.github.laxika.magicalvibes.model.effect.TargetPlayerBecomesMonarchEffect()),
                        controllerId, (UUID) null);
                trigger.setNonTargeting(true);
                gameData.enqueueTrigger(trigger);
            }
        }
    }

    private void queueInitiativeTransferFromCombatDamage(GameData gameData, CombatDamageState state,
                                                         UUID defenderId) {
        boolean combatDamageWasDealt = state.combatDamageDealtToPlayer.values().stream()
                .anyMatch(damage -> damage > 0);
        if (gameData.initiativePlayerId == null || !combatDamageWasDealt) {
            return;
        }

        Set<UUID> controllers = new LinkedHashSet<>();
        for (var damageEntry : state.combatDamageDealtToPlayer.entrySet()) {
            if (damageEntry.getValue() <= 0 || !gameData.initiativePlayerId.equals(
                    state.combatDamagePlayerRecipients.getOrDefault(damageEntry.getKey(), defenderId))) {
                continue;
            }
            UUID controllerId = state.combatDamageDealerControllers.get(damageEntry.getKey());
            if (controllerId != null && !controllerId.equals(gameData.initiativePlayerId)) {
                controllers.add(controllerId);
            }
        }
        for (UUID controllerId : controllers) {
            StackEntry trigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    null,
                    controllerId,
                    "Combat damage causes a player to take the initiative",
                    List.of(new TakeInitiativeEffect()));
            trigger.setNonTargeting(true);
            gameData.enqueueTrigger(trigger);
        }
    }

    /**
     * Processes a player's combat damage assignment for a single attacker.
     */
    public void handleCombatDamageAssigned(GameData gameData, Player player, int attackerIndex, Map<UUID, Integer> assignments) {
        if (!gameData.combatDamagePhase1Complete && !gameData.combatDamageFirstStrikeAssignmentPhase) {
            throw new IllegalStateException("Not in combat damage assignment phase");
        }

        UUID activeId = gameData.activePlayerId;
        UUID defenderId = gameQueryService.getOpponentId(gameData, activeId);

        // Route by which pending list owns this index, not by the answering player's role: banding
        // (CR 702.22j/k) can flip which player assigns. Attacker assignments (CR 510.1c) are always
        // drained before blocker divisions (CR 510.1d), so the carried index is unambiguous — the
        // carried index is a blocker's defending-battlefield index once no attacker prompts remain.
        // (Done before reading the defending battlefield so a non-pending index is rejected cleanly.)
        if (!gameData.combatDamagePendingIndices.contains(attackerIndex)) {
            if (gameData.combatDamagePendingBlockerIndices.contains(attackerIndex)) {
                handleBlockerCombatDamageAssigned(gameData, player, attackerIndex, assignments, activeId, defenderId);
                return;
            }
            throw new IllegalStateException("Attacker index " + attackerIndex + " is not pending assignment");
        }

        List<Permanent> atkBf = gameData.playerBattlefields.get(activeId);
        List<Permanent> defBf = defenderId != null ? gameData.playerBattlefields.get(defenderId) : null;
        Permanent atk = atkBf.get(attackerIndex);

        // CR 702.22j — a blocked attacker's combat damage is assigned by the defending player (not
        // the active player) when any of its living blockers has banding.
        CombatDamagePhase1State authState = gameData.combatDamagePhase1State;
        List<Integer> authLivingBlockers = new ArrayList<>();
        for (int i : authState.blockerMap.getOrDefault(attackerIndex, List.of())) {
            if (!authState.deadDefenderIndices.contains(i)) authLivingBlockers.add(i);
        }
        UUID expectedAssigner = attackerDamageAssigner(gameData, atk, authLivingBlockers, defBf, activeId, defenderId);
        if (!player.getId().equals(expectedAssigner)) {
            throw new IllegalStateException(expectedAssigner.equals(defenderId)
                    ? "Only the defending player can assign this attacker's combat damage"
                    : "Only the active player can assign combat damage");
        }

        int totalDamage = gameQueryService.getEffectiveCombatDamage(gameData, atk);

        // Validate total damage
        int totalAssigned = 0;
        for (int assigned : assignments.values()) {
            if (assigned < 0) throw new IllegalStateException("Assigned damage cannot be negative");
            totalAssigned += assigned;
        }
        if (totalAssigned != totalDamage) {
            throw new IllegalStateException("Total assigned damage (" + totalAssigned
                    + ") must equal attacker's combat damage (" + totalDamage + ")");
        }

        // Validate targets
        CombatDamagePhase1State p1 = gameData.combatDamagePhase1State;
        List<Integer> blkIndices = p1.blockerMap.get(attackerIndex);
        List<Integer> livingBlockers = new ArrayList<>();
        for (int i : blkIndices) {
            if (!p1.deadDefenderIndices.contains(i)) livingBlockers.add(i);
        }

        // Determine the overflow target: the player or planeswalker being attacked
        UUID overflowTargetId = atk.getAttackTarget() != null ? atk.getAttackTarget() : defenderId;

        // Unblocked attacker that may redirect its whole combat damage to one defending creature
        // (e.g. Cunning Giant): every defending creature (and the defending player) is a legal
        // target, but the entire combat damage must go to a single recipient (CR 510.1c — an
        // unblocked creature's combat damage isn't divided).
        boolean unblockedRedirect = livingBlockers.isEmpty()
                && !atk.isBlockedWithoutBlockers()
                && canRedirectUnblockedDamageToDefendingCreature(gameData, atk, defBf);
        boolean assignsAmongDefendingPlayerAndCreatures =
                assignsCombatDamageAmongDefendingPlayerAndCreatures(gameData, atk);

        Set<UUID> validTargetIds = new HashSet<>();
        for (int blkIdx : livingBlockers) {
            validTargetIds.add(defBf.get(blkIdx).getId());
        }
        if (unblockedRedirect) {
            for (Permanent def : defBf) {
                if (gameQueryService.isCreature(gameData, def)) {
                    validTargetIds.add(def.getId());
                }
            }
        }
        if (assignsAmongDefendingPlayerAndCreatures) {
            validTargetIds.add(defenderId);
            for (Permanent def : defBf) {
                if (gameQueryService.isCreature(gameData, def)) {
                    validTargetIds.add(def.getId());
                }
            }
        }
        boolean canTargetOverflow = gameQueryService.hasKeyword(gameData, atk, Keyword.TRAMPLE)
                || assignsCombatDamageAsThoughUnblocked(gameData, atk)
                || unblockedRedirect
                || (assignsAmongDefendingPlayerAndCreatures
                && (unblockedRedirect || (livingBlockers.isEmpty() && !atk.isBlockedWithoutBlockers())
                || Objects.equals(overflowTargetId, defenderId)));
        if (canTargetOverflow) {
            validTargetIds.add(overflowTargetId);
        }

        for (UUID targetId : assignments.keySet()) {
            if (!validTargetIds.contains(targetId)) {
                throw new IllegalStateException("Invalid damage target: " + targetId);
            }
        }

        if (unblockedRedirect && !assignsAmongDefendingPlayerAndCreatures) {
            int recipients = 0;
            for (int assigned : assignments.values()) {
                if (assigned > 0) recipients++;
            }
            if (recipients > 1) {
                throw new IllegalStateException(
                        "Unblocked combat damage must be assigned to a single recipient");
            }
        }
        if (!livingBlockers.isEmpty() && assignsCombatDamageAsThoughUnblocked(gameData, atk)
                && !assignsAmongDefendingPlayerAndCreatures
                && !gameQueryService.hasKeyword(gameData, atk, Keyword.TRAMPLE)
                && assignments.getOrDefault(overflowTargetId, 0) > 0
                && assignments.entrySet().stream().anyMatch(e -> !e.getKey().equals(overflowTargetId) && e.getValue() > 0)) {
            throw new IllegalStateException("Damage assigned as though unblocked must all be assigned to the defending target");
        }

        // Validate trample (CR 510.1c): lethal damage to each blocker is required only as a
        // precondition for assigning damage to the player or planeswalker the creature is
        // attacking. Among the blockers themselves, damage may be divided freely.
        boolean usesDefendingPlayerOrCreatureAssignment = assignsAmongDefendingPlayerAndCreatures
                && assignments.entrySet().stream().anyMatch(entry -> entry.getValue() > 0
                && (Objects.equals(entry.getKey(), defenderId)
                || defBf.stream().anyMatch(def -> def.getId().equals(entry.getKey())
                && gameQueryService.isCreature(gameData, def))));
        if (gameQueryService.hasKeyword(gameData, atk, Keyword.TRAMPLE)
                && assignments.getOrDefault(overflowTargetId, 0) > 0
                && !usesDefendingPlayerOrCreatureAssignment
                && !(assignsCombatDamageAsThoughUnblocked(gameData, atk)
                && assignments.entrySet().stream().noneMatch(e ->
                        !e.getKey().equals(overflowTargetId) && e.getValue() > 0))) {
            boolean atkHasDeathtouchForValidation = gameQueryService.hasKeyword(gameData, atk, Keyword.DEATHTOUCH);
            for (int blkIdx : livingBlockers) {
                Permanent blk = defBf.get(blkIdx);
                int alreadyTaken = blk.getMarkedDamage() + p1.defDamageTaken.getOrDefault(blkIdx, 0);
                int lethal = atkHasDeathtouchForValidation
                        ? Math.max(0, 1 - alreadyTaken)
                        : Math.max(0, gameQueryService.getLethalDamageThreshold(gameData, blk) - alreadyTaken);
                int assigned = assignments.getOrDefault(blk.getId(), 0);
                if (assigned < lethal) {
                    throw new IllegalStateException("Trample: must assign at least " + lethal
                            + " damage to " + blk.getCard().getName()
                            + " before assigning damage to the "
                            + (gameData.playerIds.contains(overflowTargetId) ? "defending player" : "planeswalker"));
                }
            }
        }

        // Validate non-trample non-unblocked: no damage to overflow target
        if (!canTargetOverflow && assignments.containsKey(overflowTargetId)) {
            throw new IllegalStateException("Cannot assign damage to " +
                    (gameData.playerIds.contains(overflowTargetId) ? "defending player" : "planeswalker"));
        }

        boolean unblocked = livingBlockers.isEmpty() && !atk.isBlockedWithoutBlockers();
        if (assignsAmongDefendingPlayerAndCreatures && unblocked
                && !Objects.equals(overflowTargetId, defenderId)
                && assignments.getOrDefault(overflowTargetId, 0) > 0
                && assignments.entrySet().stream()
                .anyMatch(entry -> !entry.getKey().equals(overflowTargetId) && entry.getValue() > 0)) {
            throw new IllegalStateException(
                    "An unblocked creature cannot split damage between its attack target and defending player or creatures");
        }
        if (assignsAmongDefendingPlayerAndCreatures && !Objects.equals(overflowTargetId, defenderId)
                && assignments.getOrDefault(overflowTargetId, 0) > 0
                && usesDefendingPlayerOrCreatureAssignment) {
            throw new IllegalStateException(
                    "Damage assigned among the defending player and creatures cannot also be assigned to the attack target");
        }

        // Store and remove from pending
        gameData.combatDamagePlayerAssignments.put(attackerIndex, assignments);
        gameData.combatDamagePendingIndices.remove(Integer.valueOf(attackerIndex));
        gameData.interaction.clearAwaitingInput();

        // Log accepted assignment
        List<String> parts = new ArrayList<>();
        for (var entry : assignments.entrySet()) {
            if (entry.getKey().equals(defenderId)) {
                parts.add(entry.getValue() + " to player");
            } else if (entry.getKey().equals(overflowTargetId) && !gameData.playerIds.contains(overflowTargetId)) {
                Permanent pw = gameQueryService.findPermanentById(gameData, overflowTargetId);
                parts.add(entry.getValue() + " to " + (pw != null ? pw.getCard().getName() : "planeswalker"));
            } else {
                Permanent target = null;
                for (Permanent p : defBf) {
                    if (p.getId().equals(entry.getKey())) {
                        target = p;
                        break;
                    }
                }
                parts.add(entry.getValue() + " to " + (target != null ? target.getCard().getName() : entry.getKey()));
            }
        }
        log.info("Game {} - Combat damage assigned for [{}]: {} -> {}", gameData.id, attackerIndex,
                atk.getCard().getName(), String.join(", ", parts));
    }

    /**
     * Processes the defending player's damage division for one creature blocking multiple
     * attackers (CR 510.1d). The division is free — the current rules impose no lethal-damage
     * ordering among the attackers — but must total the blocker's combat damage and only name
     * attackers the creature is blocking.
     */
    private void handleBlockerCombatDamageAssigned(GameData gameData, Player player, int blockerIndex,
                                                   Map<UUID, Integer> assignments,
                                                   UUID activeId, UUID defenderId) {
        if (!gameData.combatDamagePendingIndices.isEmpty()) {
            throw new IllegalStateException("Attacker damage assignments must be completed first");
        }
        if (!gameData.combatDamagePendingBlockerIndices.contains(blockerIndex)) {
            throw new IllegalStateException("Blocker index " + blockerIndex + " is not pending assignment");
        }

        List<Permanent> atkBf = gameData.playerBattlefields.get(activeId);
        List<Permanent> defBf = gameData.playerBattlefields.get(defenderId);

        // CR 702.22k — the active player (not the defending player) divides this blocker's damage
        // when the blocker is blocking any attacker with banding.
        UUID expectedAssigner = blockerDamageAssigner(gameData, blockerIndex,
                gameData.combatDamagePhase1State, atkBf, activeId, defenderId);
        if (!player.getId().equals(expectedAssigner)) {
            throw new IllegalStateException(expectedAssigner.equals(activeId)
                    ? "Only the active player can divide this blocker's combat damage"
                    : "Only the defending player can divide this blocker's combat damage");
        }

        Permanent blk = defBf.get(blockerIndex);
        int totalDamage = gameQueryService.getEffectiveCombatDamage(gameData, blk);

        int totalAssigned = 0;
        for (int assigned : assignments.values()) {
            if (assigned < 0) throw new IllegalStateException("Assigned damage cannot be negative");
            totalAssigned += assigned;
        }
        if (totalAssigned != totalDamage) {
            throw new IllegalStateException("Total assigned damage (" + totalAssigned
                    + ") must equal blocker's combat damage (" + totalDamage + ")");
        }

        CombatDamagePhase1State p1 = gameData.combatDamagePhase1State;
        Set<UUID> validTargetIds = new HashSet<>();
        for (var bEntry : p1.blockerMap.entrySet()) {
            if (p1.deadAttackerIndices.contains(bEntry.getKey())) continue;
            if (bEntry.getValue().contains(blockerIndex)) {
                validTargetIds.add(atkBf.get(bEntry.getKey()).getId());
            }
        }
        if (assignsCombatDamageAmongDefendingPlayerAndCreatures(gameData, blk)) {
            validTargetIds.add(defenderId);
            defBf.stream().filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                    .map(Permanent::getId).forEach(validTargetIds::add);
            boolean usesAlternative = assignments.entrySet().stream()
                    .anyMatch(assignment -> assignment.getValue() > 0
                            && (assignment.getKey().equals(defenderId)
                            || defBf.stream().anyMatch(permanent -> permanent.getId().equals(assignment.getKey()))));
            if (usesAlternative && assignments.entrySet().stream().anyMatch(assignment -> assignment.getValue() > 0
                    && atkBf.stream().anyMatch(permanent -> permanent.getId().equals(assignment.getKey())))) {
                throw new IllegalStateException("Cannot combine normal and alternative combat damage assignments");
            }
        }
        boolean hasTantrum = gameQueryService.hasKeyword(gameData, blk, Keyword.TANTRUM);
        if (hasTantrum) {
            validTargetIds.add(activeId);
            if (assignments.getOrDefault(activeId, 0) > 0) {
                boolean blockerHasDeathtouch = gameQueryService.hasKeyword(gameData, blk, Keyword.DEATHTOUCH);
                for (var bEntry : p1.blockerMap.entrySet()) {
                    int atkIdx = bEntry.getKey();
                    if (p1.deadAttackerIndices.contains(atkIdx) || !bEntry.getValue().contains(blockerIndex)) {
                        continue;
                    }
                    Permanent attacker = atkBf.get(atkIdx);
                    int alreadyTaken = attacker.getMarkedDamage() + p1.atkDamageTaken.getOrDefault(atkIdx, 0);
                    int lethal = blockerHasDeathtouch
                            ? Math.max(0, 1 - alreadyTaken)
                            : Math.max(0, gameQueryService.getLethalDamageThreshold(gameData, attacker) - alreadyTaken);
                    int assigned = assignments.getOrDefault(attacker.getId(), 0);
                    if (assigned < lethal) {
                        throw new IllegalStateException("Tantrum: must assign lethal damage to "
                                + attacker.getCard().getName() + " before assigning damage to its controller");
                    }
                }
            }
        }
        for (UUID targetId : assignments.keySet()) {
            if (!validTargetIds.contains(targetId)) {
                throw new IllegalStateException("Invalid damage target: " + targetId);
            }
        }

        gameData.combatDamageBlockerAssignments.put(blockerIndex, assignments);
        gameData.combatDamagePendingBlockerIndices.remove(Integer.valueOf(blockerIndex));
        gameData.interaction.clearAwaitingInput();

        List<String> parts = new ArrayList<>();
        for (var entry : assignments.entrySet()) {
            Permanent target = null;
            for (Permanent p : atkBf) {
                if (p.getId().equals(entry.getKey())) {
                    target = p;
                    break;
                }
            }
            parts.add(entry.getValue() + " to " + (target != null ? target.getCard().getName() : entry.getKey()));
        }
        log.info("Game {} - Blocker damage divided for [{}]: {} -> {}", gameData.id, blockerIndex,
                blk.getCard().getName(), String.join(", ", parts));
    }

    private Map<Integer, List<Integer>> buildBlockerMap(List<Permanent> atkBf, List<Permanent> defBf,
                                                         List<Integer> attackingIndices) {
        Map<Integer, List<Integer>> blockerMap = new LinkedHashMap<>();
        for (int atkIdx : attackingIndices) {
            Permanent attacker = atkBf.get(atkIdx);
            List<Integer> blockers = new ArrayList<>();
            for (int i = 0; i < defBf.size(); i++) {
                Permanent blocker = defBf.get(i);
                if (blocker.isBlocking()
                        && (blocker.getBlockingTargetIds().contains(attacker.getId())
                                || blocker.getBlockingTargets().contains(atkIdx))) {
                    blockers.add(i);
                }
            }
            blockerMap.put(atkIdx, blockers);
        }
        return blockerMap;
    }

    /**
     * Turns face up every face-down creature that would assign combat damage in {@code phase} and
     * turns face up when it assigns damage (Illusionary Mask). The turn-up triggers are collected by
     * the next state-based-action pass. Returns whether any creature was turned face up.
     */
    private boolean turnMaskedCombatantsFaceUp(CombatDamageState state, DamagePhase phase,
                                               DamagePhaseSnapshot snap,
                                               Map<Integer, List<Integer>> blockerMap,
                                               List<Permanent> atkBf, List<Permanent> defBf) {
        boolean turnedUp = false;
        for (var entry : blockerMap.entrySet()) {
            int atkIdx = entry.getKey();
            if (!(phase == DamagePhase.REGULAR && state.deadAttackerIndices.contains(atkIdx))) {
                turnedUp |= turnMaskedCombatantFaceUp(atkBf.get(atkIdx), snap.attackerStats().get(atkIdx), phase);
            }
            for (int blkIdx : entry.getValue()) {
                if (phase == DamagePhase.REGULAR && state.deadDefenderIndices.contains(blkIdx)) continue;
                turnedUp |= turnMaskedCombatantFaceUp(defBf.get(blkIdx), snap.defenderStats().get(blkIdx), phase);
            }
        }
        return turnedUp;
    }

    private static boolean turnMaskedCombatantFaceUp(Permanent combatant, CombatantStats stats, DamagePhase phase) {
        if (!combatant.isFaceDown() || !combatant.isTurnsFaceUpOnDamageOrTap() || stats == null
                || stats.combatDamage() <= 0 || stats.preventedFromDealingCombatDamage()
                || !stats.participatesInDamagePhase(phase)) {
            return false;
        }
        combatant.turnFaceUp();
        combatant.setPendingAutomaticTurnFaceUp(true);
        return true;
    }

    private void resolveDamagePhase(GameData gameData, CombatDamageState state,
                                     Map<Integer, List<Integer>> blockerMap,
                                     List<Permanent> atkBf, List<Permanent> defBf,
                                     UUID activeId, UUID defenderId,
                                     DamagePhase phase) {
        state.sharedRedirectShields = List.copyOf(gameData.sourcePermanentAndControllerNextDamageRedirectShields);
        state.sourceDamageShields = List.copyOf(gameData.sourceNextDamageToAnyTargetShields);
        captureCombatDamageBaselines(gameData, state, atkBf, defBf);
        // CR 510.4: all combat damage in this step is dealt simultaneously, so every value used
        // to assign and deal it (P/T, combat keywords, protection, prevention) comes from one
        // pre-damage snapshot captured under a single shared layered pass — mid-step mutations
        // (infect counters, consumed shields) must not feed back into this step's damage math.
        // Casualty determination below stays on live queries: it must see those mutations.
        DamagePhaseSnapshot initialSnap = snapshotDamagePhase(gameData, blockerMap, atkBf, defBf);
        // Illusionary Mask: a masked face-down creature that would assign combat damage is turned
        // face up first and assigns damage as the face-up creature, so re-snapshot after turning up.
        DamagePhaseSnapshot snap = turnMaskedCombatantsFaceUp(
                state, phase, initialSnap, blockerMap, atkBf, defBf)
                ? snapshotDamagePhase(gameData, blockerMap, atkBf, defBf)
                : initialSnap;

        Map<Integer, Integer> blockerDamage = precomputeBlockerDamage(
                snap, blockerMap, state.deadDefenderIndices, phase);

        // Track remaining damage for multi-blocking creatures (CR 510.1c-d)
        Map<Integer, Integer> blockerRemainingDamage = new HashMap<>(blockerDamage);

        // Living blocked-attacker count per blocker: a blocker whose blocked attackers are down
        // to one has no division to make and assigns all its combat damage to the survivor.
        Map<Integer, Integer> livingAttackersPerBlocker = new HashMap<>();
        for (var countEntry : blockerMap.entrySet()) {
            if (phase == DamagePhase.REGULAR && state.deadAttackerIndices.contains(countEntry.getKey())) continue;
            for (int blkIdx : countEntry.getValue()) {
                livingAttackersPerBlocker.merge(blkIdx, 1, Integer::sum);
            }
        }

        for (var entry : blockerMap.entrySet()) {
            int atkIdx = entry.getKey();
            List<Integer> blkIndices = entry.getValue();

            if (phase == DamagePhase.REGULAR && state.deadAttackerIndices.contains(atkIdx)) continue;

            Permanent atk = atkBf.get(atkIdx);
            CombatantStats atkStats = snap.attackerStats().get(atkIdx);
            boolean atkParticipates = atkStats.participatesInDamagePhase(phase);

            Map<UUID, Integer> playerAssignment = gameData.combatDamagePlayerAssignments.get(atkIdx);

            boolean assignAsUnblocked = assignsCombatDamageAsThoughUnblocked(gameData, atk);

            if (playerAssignment != null) {
                if (atkParticipates && !atkStats.preventedFromDealingCombatDamage()) {
                    if (phase != DamagePhase.FIRSTEST_STRIKE) {
                        applyPlayerAssignedDamage(gameData, state, atk, atkStats, defBf,
                                playerAssignment, activeId, defenderId,
                                damagePreventableFrom(gameData, snap.damagePreventable(), atk),
                                assignsCombatDamageAmongDefendingPlayerAndCreatures(gameData, atk),
                                blkIndices.isEmpty() && !atk.isBlockedWithoutBlockers());
                    }
                }
            } else if (blkIndices.isEmpty() || assignAsUnblocked) {
                // A creature made blocked without a creature blocking it deals no damage unless it
                // has trample, which can assign all its damage to the defending player.
                if (phase != DamagePhase.FIRSTEST_STRIKE
                        && atkParticipates && !atkStats.preventedFromDealingCombatDamage()
                        && (!atk.isBlockedWithoutBlockers() || atkStats.trample() || assignAsUnblocked)) {
                    int power = atkStats.combatDamage();
                    accumulatePlayerDamage(gameData, atk, atkStats, power, defenderId,
                            state, !atk.isBlockedWithoutBlockers());
                }
            } else {
                if (atkParticipates && !atkStats.preventedFromDealingCombatDamage()) {
                    distributeAttackerDamageToBlockers(gameData, state, atk, atkIdx, atkStats, blkIndices, defBf,
                            activeId, defenderId, phase, snap);
                }
            }

            if (!blkIndices.isEmpty()) {
                for (int blkIdx : blkIndices) {
                    if (phase == DamagePhase.REGULAR && state.deadDefenderIndices.contains(blkIdx)) continue;
                    Permanent blk = defBf.get(blkIdx);
                    CombatantStats blkStats = snap.defenderStats().get(blkIdx);
                    boolean blkParticipates = blkStats.participatesInDamagePhase(phase);
                    if (blkParticipates && !blkStats.preventedFromDealingCombatDamage()
                            && !(damagePreventableFrom(gameData, snap.damagePreventable(), blk)
                                    && snap.isAttackerProtectedFromBlocker(atkIdx, blkIdx))) {
                        int blkTargetCount = blk.getBlockingTargets().size();
                        Map<UUID, Integer> blockerAssignment = gameData.combatDamageBlockerAssignments.get(blkIdx);
                        int assignedDmg;
                        if (blockerAssignment != null) {
                            // CR 510.1d — the defending player's collected division for this
                            // multi-blocking creature.
                            assignedDmg = blockerAssignment.getOrDefault(atk.getId(), 0);
                        } else if (blkTargetCount <= 1) {
                            assignedDmg = blockerDamage.getOrDefault(blkIdx, 0);
                        } else {
                            int blkRemaining = blockerRemainingDamage.getOrDefault(blkIdx, 0);
                            if (livingAttackersPerBlocker.getOrDefault(blkIdx, 0) <= 1) {
                                // Only one blocked attacker remains in combat: all the blocker's
                                // damage is assigned to it — there is no division to make.
                                assignedDmg = blkRemaining;
                            } else {
                                // No collected division (e.g. AI shortcut path): fall back to
                                // lethal-in-order, a legal default division.
                                int atkDamageSoFar = atk.getMarkedDamage() + state.atkDamageTaken.getOrDefault(atkIdx, 0);
                                int lethalNeeded = blkStats.deathtouch()
                                        ? Math.max(0, 1 - atkDamageSoFar)
                                        : Math.max(0, atkStats.lethalDamageThreshold() - atkDamageSoFar);
                                assignedDmg = Math.min(blkRemaining, lethalNeeded);
                            }
                            blockerRemainingDamage.put(blkIdx, blkRemaining - assignedDmg);
                        }
                        int actualDmg = assignedDmg;
                        if (!applyCombatCreatureDamage(gameData, state, blk, blkStats, atk, actualDmg)) {
                            state.combatDamageDealt.merge(blk, actualDmg, Integer::sum);
                            recordCombatDamageToCreature(gameData, state, blk, defenderId, atk, actualDmg);
                        }
                    }
                }
            }
        }

        for (var assignmentEntry : gameData.combatDamageBlockerAssignments.entrySet()) {
            int blockerIndex = assignmentEntry.getKey();
            Permanent blocker = defBf.get(blockerIndex);
            CombatantStats stats = snap.defenderStats().get(blockerIndex);
            if (!assignsCombatDamageAmongDefendingPlayerAndCreatures(gameData, blocker)
                    && !gameQueryService.hasKeyword(gameData, blocker, Keyword.TANTRUM)
                    || stats == null || !stats.participatesInDamagePhase(phase)
                    || stats.preventedFromDealingCombatDamage()
                    || (phase == DamagePhase.REGULAR && state.deadDefenderIndices.contains(blockerIndex))) continue;
            Map<UUID, Integer> assignments = assignmentEntry.getValue();
            if (gameQueryService.hasKeyword(gameData, blocker, Keyword.TANTRUM)) {
                int playerDamage = assignments.getOrDefault(activeId, 0);
                if (playerDamage > 0 && phase != DamagePhase.FIRSTEST_STRIKE) {
                    accumulatePlayerDamageToPlayer(gameData, blocker, stats, playerDamage, activeId, state, false);
                }
                continue;
            }
            int playerDamage = assignments.getOrDefault(defenderId, 0);
            if (playerDamage > 0 && phase != DamagePhase.FIRSTEST_STRIKE) {
                accumulatePlayerDamageToPlayer(gameData, blocker, stats, playerDamage, defenderId, state, false);
            }
            for (int targetIndex = 0; targetIndex < defBf.size(); targetIndex++) {
                Permanent target = defBf.get(targetIndex);
                int damage = assignments.getOrDefault(target.getId(), 0);
                if (damage <= 0 || (damagePreventableFrom(gameData, snap.damagePreventable(), blocker)
                        && gameQueryService.hasProtectionFromDamageSource(gameData, target, blocker))) continue;
                if (!applyCombatCreatureDamage(gameData, state, blocker, stats, target, damage)) {
                    state.combatDamageDealt.merge(blocker, damage, Integer::sum);
                    recordCombatDamageToCreature(gameData, state, blocker, defenderId, target, damage);
                }
            }
        }
        processCollectedCombatDamage(gameData, state);
    }

    // Every original combat assignment belongs to the same damage event. Reuse its shields for
    // those assignments only; redirected damage still sees a consumed shield, preventing loops.
    private void restoreSharedRedirectForCombatChunk(GameData gameData, CombatDamageState state,
                                                     UUID targetId, boolean playerTarget) {
        for (SourcePermanentAndControllerNextDamageRedirectShield shield : state.sharedRedirectShields.reversed()) {
            UUID protectedId = playerTarget ? shield.protectedPlayerId() : shield.protectedPermanentId();
            if (targetId.equals(protectedId)
                    && !gameData.sourcePermanentAndControllerNextDamageRedirectShields.contains(shield)) {
                gameData.sourcePermanentAndControllerNextDamageRedirectShields.addFirst(shield);
            }
        }
    }

    private void restoreSourceShieldForCombatChunk(GameData gameData, CombatDamageState state,
                                                    UUID sourceId, UUID recipientId, boolean playerTarget) {
        for (SourceNextDamageToAnyTargetShield shield : state.sourceDamageShields.reversed()) {
            if (shield.remainingDamage() == Integer.MAX_VALUE
                    && sourceId.equals(shield.sourceId())
                    && (shield.recipientId() == null || recipientId.equals(shield.recipientId()))
                    && (!shield.playersOnly() || playerTarget)
                    && !gameData.sourceNextDamageToAnyTargetShields.contains(shield)) {
                gameData.sourceNextDamageToAnyTargetShields.addFirst(shield);
            }
        }
    }

    /**
     * Pre-damage view of one combat participant, captured before any combat damage of the
     * current damage step is applied (CR 510.4 — damage is dealt simultaneously, so assignment
     * and dealing use the pre-damage board even when earlier loop iterations placed infect
     * counters or removed permanents).
     */
    private record CombatantStats(int combatDamage, int lethalDamageThreshold,
                                  boolean firstestStrike,
                                  boolean firstStrike,
                                  boolean doubleStrike, boolean deathtouch, boolean trample,
                                  boolean infect, boolean preventedFromDealingCombatDamage,
                                  CardColor color) {

        boolean participatesInDamagePhase(DamagePhase phase) {
            return switch (phase) {
                case FIRSTEST_STRIKE -> firstestStrike;
                case FIRST_STRIKE -> firstStrike || doubleStrike;
                case REGULAR -> (!firstStrike && !firstestStrike) || doubleStrike;
            };
        }
    }

    /** Pre-damage stats for every combat participant plus the per-pair protection matrix. */
    private record DamagePhaseSnapshot(Map<Integer, CombatantStats> attackerStats,
                                       Map<Integer, CombatantStats> defenderStats,
                                       boolean damagePreventable,
                                       Set<Long> attackerProtectedFromBlocker,
                                       Set<Long> blockerProtectedFromAttacker) {

        private static long pairKey(int atkIdx, int blkIdx) {
            return ((long) atkIdx << 32) | (blkIdx & 0xFFFFFFFFL);
        }

        boolean isAttackerProtectedFromBlocker(int atkIdx, int blkIdx) {
            return attackerProtectedFromBlocker.contains(pairKey(atkIdx, blkIdx));
        }

        boolean isBlockerProtectedFromAttacker(int atkIdx, int blkIdx) {
            return blockerProtectedFromAttacker.contains(pairKey(atkIdx, blkIdx));
        }
    }

    /** Captures {@link CombatantStats} for every attacker and blocker under one shared layered pass. */
    private DamagePhaseSnapshot snapshotDamagePhase(GameData gameData,
                                                    Map<Integer, List<Integer>> blockerMap,
                                                    List<Permanent> atkBf, List<Permanent> defBf) {
        return gameQueryService.withQueryScope(gameData, () -> {
            Map<Integer, CombatantStats> attackerStats = new HashMap<>();
            Map<Integer, CombatantStats> defenderStats = new HashMap<>();
            Set<Long> attackerProtectedFromBlocker = new HashSet<>();
            Set<Long> blockerProtectedFromAttacker = new HashSet<>();
            for (var entry : blockerMap.entrySet()) {
                int atkIdx = entry.getKey();
                Permanent atk = atkBf.get(atkIdx);
                attackerStats.put(atkIdx, snapshotCombatant(gameData, atk));
                for (int blkIdx : entry.getValue()) {
                    Permanent blk = defBf.get(blkIdx);
                    if (!defenderStats.containsKey(blkIdx)) {
                        defenderStats.put(blkIdx, snapshotCombatant(gameData, blk));
                    }
                    if (gameQueryService.hasProtectionFromDamageSource(gameData, atk, blk)) {
                        attackerProtectedFromBlocker.add(DamagePhaseSnapshot.pairKey(atkIdx, blkIdx));
                    }
                    if (gameQueryService.hasProtectionFromDamageSource(gameData, blk, atk)) {
                        blockerProtectedFromAttacker.add(DamagePhaseSnapshot.pairKey(atkIdx, blkIdx));
                    }
                }
            }
            return new DamagePhaseSnapshot(attackerStats, defenderStats,
                    gameQueryService.isDamagePreventable(gameData, true),
                    attackerProtectedFromBlocker, blockerProtectedFromAttacker);
        });
    }

    private CombatantStats snapshotCombatant(GameData gameData, Permanent creature) {
        return new CombatantStats(
                gameQueryService.getEffectiveCombatDamage(gameData, creature),
                gameQueryService.getLethalDamageThreshold(gameData, creature),
                gameQueryService.hasActiveStaticEffect(gameData, creature, FirstestStrikeEffect.class),
                gameQueryService.hasKeyword(gameData, creature, Keyword.FIRST_STRIKE),
                gameQueryService.hasKeyword(gameData, creature, Keyword.DOUBLE_STRIKE),
                gameQueryService.hasKeyword(gameData, creature, Keyword.DEATHTOUCH),
                gameQueryService.hasKeyword(gameData, creature, Keyword.TRAMPLE),
                gameQueryService.hasKeyword(gameData, creature, Keyword.INFECT),
                gameData.creaturesAssigningNoCombatDamageThisTurn.contains(creature.getId())
                        || gameData.creaturesAssigningNoCombatDamageThisCombat.contains(creature.getId())
                        || gameQueryService.isPreventedFromDealingDamage(gameData, creature, true),
                gameQueryService.getEffectiveColor(gameData, creature));
    }

    private void distributeAttackerDamageToBlockers(GameData gameData, CombatDamageState state,
                                                     Permanent atk, int atkIdx, CombatantStats atkStats,
                                                     List<Integer> blkIndices,
                                                     List<Permanent> defBf,
                                                     UUID activeId, UUID defenderId,
                                                     DamagePhase phase, DamagePhaseSnapshot snap) {
        boolean atkHasDeathtouch = atkStats.deathtouch();
        int remaining = atkStats.combatDamage();
        List<Integer> damageRecipients = blkIndices.stream()
                .filter(blkIdx -> phase != DamagePhase.REGULAR || !state.deadDefenderIndices.contains(blkIdx))
                .toList();
        for (int i = 0; i < damageRecipients.size(); i++) {
            int blkIdx = damageRecipients.get(i);
            Permanent blk = defBf.get(blkIdx);
            int blockerDamageSoFar = blk.getMarkedDamage() + state.defDamageTaken.getOrDefault(blkIdx, 0);
            int lethalNeeded = atkHasDeathtouch
                    ? Math.max(0, 1 - blockerDamageSoFar)
                    : snap.defenderStats().get(blkIdx).lethalDamageThreshold() - blockerDamageSoFar;
            int dmg = i == damageRecipients.size() - 1 && !atkStats.trample()
                    ? remaining
                    : Math.min(remaining, Math.max(0, lethalNeeded));
            if (!(damagePreventableFrom(gameData, snap.damagePreventable(), atk)
                    && snap.isBlockerProtectedFromAttacker(atkIdx, blkIdx))) {
                int actualDmg = dmg;
                if (!applyCombatCreatureDamage(gameData, state, atk, atkStats, blk, actualDmg)) {
                    state.combatDamageDealt.merge(atk, actualDmg, Integer::sum);
                    recordCombatDamageToCreature(gameData, state, atk, activeId, blk, actualDmg);
                }
            }
            remaining -= dmg;
        }
        if (remaining > 0 && atkStats.trample() && phase != DamagePhase.FIRSTEST_STRIKE) {
            int doubledRemaining = remaining;
            accumulatePlayerDamage(gameData, atk, atkStats, doubledRemaining, defenderId, state, false);
        }
    }

    private void applyPlayerAssignedDamage(GameData gameData, CombatDamageState state,
                                            Permanent atk, CombatantStats atkStats,
                                            List<Permanent> defBf,
                                            Map<UUID, Integer> playerAssignment,
                                            UUID activeId, UUID defenderId,
                                            boolean damagePreventable,
                                            boolean assignsAmongDefendingPlayerAndCreatures,
                                            boolean sourceUnblocked) {
        UUID overflowTargetId = atk.getAttackTarget() != null ? atk.getAttackTarget() : defenderId;
        for (var dmgEntry : playerAssignment.entrySet()) {
            UUID targetId = dmgEntry.getKey();
            int dmg = dmgEntry.getValue();
            if (dmg <= 0) continue;
            boolean assignedToDefendingPlayer = assignsAmongDefendingPlayerAndCreatures
                    && targetId.equals(defenderId) && !targetId.equals(overflowTargetId);
            if (targetId.equals(overflowTargetId) || assignedToDefendingPlayer) {
                // Overflow damage to the attack target (player or planeswalker)
                int actualDmg = dmg;
                if (assignedToDefendingPlayer) {
                    accumulatePlayerDamageToPlayer(gameData, atk, atkStats, actualDmg, defenderId,
                            state, sourceUnblocked);
                } else {
                    accumulatePlayerDamage(gameData, atk, atkStats, actualDmg, defenderId,
                            state, sourceUnblocked);
                }
            } else {
                // Damage assigned to a defending creature. For a blocked attacker this is one of
                // its blockers; for an unblocked redirect (e.g. Cunning Giant) it can be any
                // defending creature, so search the whole defending battlefield by index.
                int targetIdx = -1;
                for (int i = 0; i < defBf.size(); i++) {
                    if (defBf.get(i).getId().equals(targetId)) {
                        targetIdx = i;
                        break;
                    }
                }
                if (targetIdx >= 0) {
                    Permanent blk = defBf.get(targetIdx);
                    if (!(damagePreventable && gameQueryService.hasProtectionFromDamageSource(gameData, blk, atk))) {
                        int actualDmg = dmg;
                        if (!applyCombatCreatureDamage(gameData, state, atk, atkStats, blk, actualDmg)) {
                            state.combatDamageDealt.merge(atk, actualDmg, Integer::sum);
                            recordCombatDamageToCreature(gameData, state, atk, activeId, blk, actualDmg);
                        }
                    }
                }
            }
        }
    }

    private void processLifelink(GameData gameData, Map<Permanent, Integer> combatDamageDealt,
                                 Set<Permanent> lifelinkDealers) {
        for (var entry : combatDamageDealt.entrySet()) {
            Permanent creature = entry.getKey();
            int damageDealt = entry.getValue();
            if (damageDealt <= 0) continue;
            if (!lifelinkDealers.contains(creature)) continue;
            UUID controllerId = gameData.findControllerOf(creature);
            if (controllerId == null) continue;
            lifeSupport.applyGainLife(gameData, controllerId, damageDealt, "lifelink");
        }
    }

    private void processGainLifeEqualToDamageDealt(GameData gameData, Map<Permanent, Integer> combatDamageDealt) {
        for (var entry : combatDamageDealt.entrySet()) {
            Permanent creature = entry.getKey();
            int damageDealt = entry.getValue();
            if (damageDealt <= 0) continue;
            gameData.forEachPermanent((playerId, perm) -> {
                if (perm.isAttached() && perm.getAttachedTo().equals(creature.getId())) {
                    for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                        if (effect instanceof GainLifeEqualToDamageDealtEffect) {
                            lifeSupport.applyGainLife(gameData, playerId, damageDealt, perm.getCard().getName(),
                                    perm.getCard(), StackEntryType.TRIGGERED_ABILITY, playerId);
                        }
                    }
                }
            });
        }
    }

    private void processGainLifeEqualToControlledCreatureCombatDamage(GameData gameData, Map<Permanent, Integer> combatDamageDealt) {
        for (var entry : combatDamageDealt.entrySet()) {
            Permanent creature = entry.getKey();
            int damageDealt = entry.getValue();
            if (damageDealt <= 0) continue;
            UUID controllerId = gameData.findControllerOf(creature);
            if (controllerId == null) continue;
            // "Whenever a creature you control deals combat damage, you gain that much life."
            // Fires once per matching enchantment the creature's controller controls (Noble Purpose).
            gameData.forEachPermanent((ownerId, perm) -> {
                for (CardEffect effect : gameQueryService.getActiveStaticEffects(gameData, perm)) {
                    if (effect instanceof GainLifeEqualToControlledCreatureCombatDamageEffect) {
                        UUID enchantmentControllerId = gameQueryService.findPermanentController(gameData, perm.getId());
                        if (controllerId.equals(enchantmentControllerId)) {
                            StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                                    perm.getCard(), controllerId, perm.getCard().getName() + "'s ability",
                                    List.of(new com.github.laxika.magicalvibes.model.effect.GainLifeEffect(damageDealt)),
                                    (UUID) null, perm.getId());
                            trigger.setNonTargeting(true);
                            gameData.enqueueTrigger(trigger);
                        }
                    }
                }
            });
        }
    }

    /**
     * Queues ON_ANY_SOURCE_DEALS_DAMAGE reflection triggers (Justice) for each creature that dealt
     * combat damage this step. {@code combatDamageDealt} is keyed by the damage-source creature and
     * already sums the damage it dealt to every recipient (creatures and players), matching the
     * "add up all the damage done and deal it at one time" ruling.
     */
    /**
     * Accumulates the per-turn "damage this source has dealt" totals for combat damage. Combat damage
     * never routes through {@code DamageSupport}, which records the noncombat side, so this is the
     * combat half of the same counter (Chandra, Fire of Kaladesh). {@code combatDamageDealt} is already
     * summed per source across every recipient in this step, so no double counting occurs.
     */
    private void recordCombatDamageBySource(GameData gameData, CombatDamageState state) {
        for (var entry : state.combatDamageDealt.entrySet()) {
            gameData.recordDamageDealtBySource(entry.getKey().getId(), entry.getValue());
            UUID sourceControllerId = state.combatDamageDealerControllers.get(entry.getKey());
            if (sourceControllerId == null) {
                sourceControllerId = gameQueryService.findPermanentController(gameData, entry.getKey().getId());
            }
            gameData.recordDamageSourceControlledBy(entry.getKey().getId(), sourceControllerId);
        }
    }

    private void processSourceDealsDamageReflectionTriggers(GameData gameData, CombatDamageState state,
                                                             UUID defenderId,
                                                             Map<UUID, Permanent> damagedCreatureSnapshots) {
        for (var entry : state.combatDamageDealt.entrySet()) {
            Permanent source = entry.getKey();
            int damageDealt = entry.getValue();
            if (damageDealt <= 0) continue;
            // Prefer the controller captured while the source was alive (it may have died dealing
            // damage to a blocker); fall back to a live lookup for still-present sources.
            UUID controllerId = state.combatDamageDealerControllers.get(source);
            if (controllerId == null) controllerId = gameData.findControllerOf(source);
            if (controllerId == null) continue;
            int damageToDefender = state.combatDamageDealtToPlayer.getOrDefault(source, 0);
            Map<UUID, Integer> damageToPermanents = new HashMap<>();
            state.combatDamageAmountsToCreatures.getOrDefault(source, Map.of())
                    .forEach((permanentId, damage) -> damageToPermanents.merge(permanentId, damage, Integer::sum));
            state.combatDamageAmountsToPlaneswalkers.getOrDefault(source, Map.of())
                    .forEach((permanentId, damage) -> damageToPermanents.merge(permanentId, damage, Integer::sum));
            state.combatDamageAmountsToBattles.getOrDefault(source, Map.of())
                    .forEach((permanentId, damage) -> damageToPermanents.merge(permanentId, damage, Integer::sum));
            triggerCollectionService.queueSourceDealsDamageReflections(
                    gameData, source.getCard(), controllerId, source.getId(), damageDealt,
                    damageToDefender > 0 ? Map.of(
                            state.combatDamagePlayerRecipients.getOrDefault(source, defenderId),
                            damageToDefender) : Map.of(),
                    state.selfDealsDamageEffects.get(source), null, damageToPermanents, true,
                    damagedCreatureSnapshots);
        }
    }

    private void snapshotSelfDealsDamageEffects(GameData gameData, CombatDamageState state) {
        for (Permanent source : state.combatDamageDealt.keySet()) {
            List<CardEffect> effects = new ArrayList<>(source.getCard().getEffects(EffectSlot.ON_SELF_DEALS_DAMAGE));
            effects.addAll(source.getTemporaryTriggeredEffects(EffectSlot.ON_SELF_DEALS_DAMAGE));
            effects.addAll(source.getPersistentTriggeredEffects(EffectSlot.ON_SELF_DEALS_DAMAGE));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, source, EffectSlot.ON_SELF_DEALS_DAMAGE));
            state.selfDealsDamageEffects.put(source, effects);
        }
    }

    private void processSelfDealsCombatDamageTriggers(GameData gameData, CombatDamageState state,
                                                       UUID defenderId) {
        for (var entry : state.combatDamageDealt.entrySet()) {
            Permanent source = entry.getKey();
            int damageDealt = entry.getValue();
            if (damageDealt <= 0) continue;

            // The dealer's controller is only captured on the creature-damage path, so fall back to a
            // live lookup for a creature whose damage went to a player or planeswalker only.
            UUID controllerId = state.combatDamageDealerControllers.get(source);
            if (controllerId == null) controllerId = gameData.findControllerOf(source);
            if (controllerId == null) continue;
            triggerCollectionService.queueSourceDealsCombatDamageTriggers(
                    gameData, source.getCard(), controllerId, source.getId(), damageDealt,
                    state.combatDamageDealtToPlayer.getOrDefault(source, 0),
                    state.combatDamagePlayerRecipients.getOrDefault(source, defenderId),
                    state.selfDealsCombatDamageEffects.get(source));
        }
    }

    private void processCombatDamageToPlayerTriggers(GameData gameData, Map<Permanent, Integer> combatDamageDealtToPlayer,
                                                     UUID attackerId, UUID defenderId, Map<Permanent, UUID> attachedDamageSources,
                                                     List<TriggerCollectionService.AllyDamageTriggerSnapshot> allyDamageWatchers,
                                                     Map<UUID, List<Permanent>> departedWatchersByController) {
        // Sources of batched ally triggers ("whenever one or more ... deal combat damage to a
        // player") that already fired in this damage step against this player, so they fire once
        // for the whole batch instead of once per dealer.
        Set<UUID> firedBatchedAllyTriggerSources = new HashSet<>();
        triggerCollectionService.checkControllerDealtCombatDamageTriggers(gameData, defenderId,
                combatDamageDealtToPlayer.values().stream().mapToInt(Integer::intValue).sum());
        triggerCollectionService.checkGoadedCreatureCombatDamageToOpponentTriggers(
                gameData,
                defenderId,
                combatDamageDealtToPlayer.entrySet().stream()
                        .filter(entry -> entry.getValue() > 0)
                        .map(Map.Entry::getKey)
                        .toList());
        triggerCollectionService.checkAllyCreaturesDealDamageToOpponentTriggers(
                gameData, attackerId, defenderId,
                combatDamageDealtToPlayer.entrySet().stream()
                        .filter(entry -> entry.getValue() > 0)
                        .map(Map.Entry::getKey)
                        .toList(), allyDamageWatchers);
        triggerCollectionService.checkAllyCreaturesDealDamageToPlayerTriggers(
                gameData, attackerId, defenderId,
                combatDamageDealtToPlayer.entrySet().stream()
                        .filter(entry -> entry.getValue() > 0)
                        .map(Map.Entry::getKey)
                        .toList());
        // Graveyard cards whose "whenever one or more creatures you control deal combat damage"
        // trigger has already fired for this damage event — such a trigger fires only once no
        // matter how many creatures connected.
        for (var entry : combatDamageDealtToPlayer.entrySet()) {
            Permanent creature = entry.getKey();
            int damageDealt = entry.getValue();
            if (damageDealt <= 0) continue;

            gameData.recordCombatDamageByCreatureNameToPlayer(creature.getCard().getName(), defenderId);
            gameData.combatDamageToPlayersThisTurn
                    .computeIfAbsent(creature.getId(), k -> ConcurrentHashMap.newKeySet())
                    .add(defenderId);
            gameData.combatDamageToPlayersThisCombat
                    .computeIfAbsent(creature.getId(), k -> ConcurrentHashMap.newKeySet())
                    .add(defenderId);
            gameData.playersDealtCombatDamageSinceTheirLastTurn.add(defenderId);
            gameData.recordCreatureDamageSourceToPlayer(creature.getId(), defenderId);
            if (gameData.isCommander(creature.getOriginalCard().getId())) {
                gameData.combatDamageSourcesThatWereCommandersThisTurn.add(creature.getId());
            }
            if (gameQueryService.hasEffectiveSupertype(gameData, creature, CardSupertype.LEGENDARY)) {
                gameData.combatDamageSourcesWithLegendaryThisTurn.add(creature.getId());
                gameData.legendaryCreatureCombatDamageToPlayersThisTurn
                        .computeIfAbsent(creature.getId(), ignored -> ConcurrentHashMap.newKeySet())
                        .add(defenderId);
            }

            // Record creature subtypes at combat damage time for subtype-conditional triggers
            // (e.g. Admiral Beckett Brass checks if 3+ Pirates dealt damage to a player)
            gameData.combatDamageSourceNamesThisTurn.putIfAbsent(creature.getId(), creature.getCard().getName());
            {
                Set<CardSubtype> effectiveSubtypes = gameData.combatDamageSourceSubtypesThisTurn
                        .computeIfAbsent(creature.getId(), ignored -> ConcurrentHashMap.newKeySet());
                effectiveSubtypes.addAll(creature.getCard().getSubtypes());
                effectiveSubtypes.addAll(creature.getGrantedSubtypes());
                effectiveSubtypes.addAll(creature.getTransientSubtypes());
                if (gameQueryService.hasKeyword(gameData, creature, Keyword.CHANGELING)) {
                    gameData.combatDamageSourcesWithChangelingThisTurn.add(creature.getId());
                }
            }

            // Record the controller's subtypes at combat damage time so prowl (an alternative
            // cost gated on "you dealt combat damage to a player this turn with a [subtype]") can be
            // evaluated regardless of whether the source is still on the battlefield.
            gameData.combatDamageToPlayerControllerSubtypesThisTurn
                    .computeIfAbsent(attackerId, k -> ConcurrentHashMap.newKeySet())
                    .addAll(gameData.combatDamageSourceSubtypesThisTurn.get(creature.getId()));
            gameData.combatDamageDealersByControllerThisTurn
                    .computeIfAbsent(attackerId, k -> ConcurrentHashMap.newKeySet())
                    .add(creature.getId());
            if (gameData.combatDamageSourcesWithChangelingThisTurn.contains(creature.getId())) {
                gameData.controllersDealtCombatDamageWithChangelingThisTurn.add(attackerId);
            }

            List<CardEffect> allDamageEffects = new ArrayList<>();
            // A creature that has lost all abilities also loses the abilities it was permanently granted
            // earlier (e.g. an encoded cipher spell's trigger).
            boolean lostAllAbilities = creature.isLosesAllAbilitiesUntilEndOfTurn()
                    || gameQueryService.computeStaticBonus(gameData, creature).losesAllAbilities();
            // An animated noncreature Equipment that is not attached is not an equipped creature, so its
            // printed "equipped creature deals combat damage" triggers do not apply. Equipment creatures
            // (reconfigure) keep theirs: they read "this creature or equipped creature".
            boolean unattachedEquipment = creature.getAttachedTo() == null
                    && GameQueryService.permanentHasSubtype(creature, CardSubtype.EQUIPMENT)
                    && !creature.getCard().hasType(CardType.CREATURE);
            boolean hasPrintedAbilities = !creature.isFaceDown() && !lostAllAbilities && !unattachedEquipment;
            if (hasPrintedAbilities) {
                allDamageEffects.addAll(creature.getCard().getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER));
                allDamageEffects.addAll(creature.getCard().getEffects(EffectSlot.ON_DAMAGE_TO_PLAYER));
            }
            if (!attackerId.equals(defenderId)) {
                if (hasPrintedAbilities) {
                    allDamageEffects.addAll(creature.getCard().getEffects(EffectSlot.ON_DAMAGE_TO_OPPONENT));
                }
                allDamageEffects.addAll(creature.getTemporaryTriggeredEffects(EffectSlot.ON_DAMAGE_TO_OPPONENT));
                if (!lostAllAbilities) {
                    allDamageEffects.addAll(creature.getPersistentTriggeredEffects(EffectSlot.ON_DAMAGE_TO_OPPONENT));
                }
                allDamageEffects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                        gameData, creature, EffectSlot.ON_DAMAGE_TO_OPPONENT));
            }
            // Combat-damage-to-player triggers granted until end of turn by one-shot effects
            // (e.g. Open into Wonder grants "deals combat damage to a player, draw a card").
            allDamageEffects.addAll(creature.getTemporaryTriggeredEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER));
            if (!lostAllAbilities) {
                allDamageEffects.addAll(creature.getPersistentTriggeredEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER));
                allDamageEffects.addAll(creature.getPersistentTriggeredEffects(EffectSlot.ON_DAMAGE_TO_PLAYER));
            }
            // Combat-damage-to-player triggers granted continuously by a static ability
            // (e.g. Tandem Lookout's soulbond grant), recomputed by the layer system.
            allDamageEffects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, creature, EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER));
            allDamageEffects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, creature, EffectSlot.ON_DAMAGE_TO_PLAYER));
            TriggerContext.SourceDealsCombatDamage combatDamageContext = new TriggerContext.SourceDealsCombatDamage(
                    creature.getCard(), attackerId, creature.getId(), damageDealt, damageDealt);
            int creatureTriggerCopies = 1
                    + gameQueryService.countAdditionalTriggeredAbilityTriggers(
                    gameData, attackerId, creature, false, combatDamageContext);
            int previousCopies = gameData.beginTriggeredAbilityCopies(creatureTriggerCopies);
            try {
            for (CardEffect candidateEffect : allDamageEffects) {
                // A delayed trigger another permanent created for this creature is not an ability
                // of the creature, so abilities that add triggers for the creature never copy it.
                gameData.beginTriggeredAbilityCopies(
                        candidateEffect instanceof com.github.laxika.magicalvibes.model.effect.TransformGrantingPermanentEffect
                                ? 1 : creatureTriggerCopies);
                if (candidateEffect instanceof com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfCombatDamageSourceIntoHandEffect
                        && creature.getCard().isToken()) continue;
                if (candidateEffect instanceof com.github.laxika.magicalvibes.model.effect.DamagedPlayerOnlyEffect onlyDamagedPlayer
                        && !defenderId.equals(onlyDamagedPlayer.playerId())) continue;
                CardEffect rawEffect = candidateEffect instanceof com.github.laxika.magicalvibes.model.effect.DamagedPlayerOnlyEffect onlyDamagedPlayer
                        ? onlyDamagedPlayer.wrapped() : candidateEffect;
                if (rawEffect instanceof com.github.laxika.magicalvibes.model.effect.RenownEffect
                        && creature.isRenowned()) continue;
                CardEffect effect = rawEffect instanceof CombatDamageAmountAwareEffect amountAware
                        ? amountAware.snapshotCombatDamage(damageDealt)
                        : rawEffect;
                // CR 603.4 — only an intervening "if" clause gates trigger creation. Resolution-
                // time conditions remain on the stack and are checked by EffectResolutionService.
                if (effect instanceof ConditionalEffect conditional
                        && conditional.interveningIf()
                        && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forPermanent(creature, attackerId)
                                        .withTargetId(defenderId))) {
                    log.info("Game {} - {}'s {} combat damage trigger does not fire", gameData.id,
                            creature.getCard().getName(), conditional.conditionName());
                    continue;
                }

                if (effect instanceof ConditionalEffect metalcraft && metalcraft.condition() instanceof Metalcraft) {
                    StackEntry se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            creature.getCard().getName() + "'s triggered ability",
                            List.of(metalcraft), defenderId, creature.getId());
                    se.setNonTargeting(true);
                    gameData.stack.add(se);
                    gameLogService.append(gameData, GameLog.cardThen(creature.getCard(), "'s metalcraft ability triggers:"));
                    continue;
                }

                if (effect instanceof LoseGameIfSourceDealtDamageToPlayerThisTurnEffect threshold
                        && gameData.damageDealtBySourceToPlayerThisTurn(
                        creature.getId(), defenderId) < threshold.minimumDamage()) {
                    continue;
                }
                if (effect instanceof com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect modal) {
                    gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                            creature.getCard(), attackerId, modal.choice(), creature.getId(),
                            false, false, null, defenderId));
                    gameLogService.append(gameData, GameLog.abilityTriggers(creature.getCard()));
                    continue;
                }

                if (effect instanceof ChooseModeNotYetChosenEffect modal) {
                    gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                            creature.getCard(), attackerId, new ChooseOneEffect(modal.options()),
                            creature.getId(), false, true, null));
                    gameLogService.append(gameData, GameLog.abilityTriggers(creature.getCard()));
                    continue;
                }

                if (effect instanceof com.github.laxika.magicalvibes.model.effect
                        .ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect) {
                    List<Card> randomCandidates = gameData.playerGraveyards.getOrDefault(attackerId, List.of()).stream()
                            .filter(card -> card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))
                            .toList();
                    if (randomCandidates.isEmpty()) {
                        continue;
                    }
                    Card chosenCard = randomCandidates.get(
                            java.util.concurrent.ThreadLocalRandom.current().nextInt(randomCandidates.size()));
                    StackEntry randomSe = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            creature.getCard().getName() + "'s triggered ability",
                            List.of(new com.github.laxika.magicalvibes.model.effect
                                    .ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect(chosenCard.getId())),
                            null, creature.getId());
                    randomSe.setSourcePermanentSnapshot(new Permanent(creature));
                    randomSe.setNonTargeting(true);
                    gameData.stack.add(randomSe);
                    gameLogService.append(gameData, GameLog.cardThen(creature.getCard(),
                            "'s combat damage trigger targets " + chosenCard.getName() + " at random."));
                    continue;
                }

                if (effect instanceof TargetPlayerLosesGameEffect) {
                    gameData.stack.add(new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            creature.getCard().getName() + "'s triggered ability",
                            List.of(new TargetPlayerLosesGameEffect(defenderId)), null, creature.getId()));
                    gameLogService.append(gameData, GameLog.cardThen(creature.getCard(), "'s ability triggers \u2014 " + gameData.playerIdToName.get(defenderId) + " loses the game."));
                    continue;
                }

                if (effect instanceof ReturnPermanentsOnCombatDamageToPlayerEffect bounce
                        && bounce.fixedCount() == 0) {
                    gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                            creature.getCard(), attackerId, List.of(bounce.withTriggerTimeTargets()),
                            creature.getId(), attackerId, defenderId, null, damageDealt));
                    gameLogService.append(gameData, GameLog.abilityTriggers(creature.getCard()));
                    continue;
                }

                if (effect instanceof MayEffect may) {
                    if (may.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                        // An opponent-graveyard scope on a combat damage trigger means the damaged
                        // player's graveyard (Ink-Eyes, "that player's graveyard").
                        UUID mayGraveyardOwnerId =
                                may.targetSpec().graveyardScope().orElse(null) == GraveyardSearchScope.OPPONENT_GRAVEYARD
                                        ? defenderId : attackerId;
                        gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                                creature.getCard(), attackerId, List.of(effect), mayGraveyardOwnerId,
                                1, 0, 1, null, false, null, creature.getId()));
                        gameLogService.append(gameData, GameLog.cardThen(creature.getCard(),
                                "'s combat damage trigger goes on the stack — choose a graveyard target."));
                        continue;
                    }
                    if (may.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                            || may.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
                        if (triggerCollectionService.needsSlotBySlotTargetSelection(creature.getCard())) {
                            gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                                    creature.getCard(), attackerId, List.of(effect), creature.getId(), List.of(), 0, 0));
                        } else {
                            gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                                    creature.getCard(), attackerId, List.of(effect), creature.getId(),
                                    attackerId, defenderId));
                        }
                        gameLogService.append(gameData, GameLog.cardThen(creature.getCard(),
                                "'s combat damage trigger goes on the stack — choose a target."));
                        continue;
                    }
                    MayEffect queuedMay = may;
                    if (may.wrapped() instanceof SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect
                            || may.wrapped() instanceof TransformSelfAndAttachToCreatureDamagedPlayerControlsEffect) {
                        List<Permanent> defenderBf = gameData.playerBattlefields.get(defenderId);
                        List<UUID> creatureTargetIds = defenderBf == null ? List.of() : defenderBf.stream()
                                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                                .filter(permanent -> !gameQueryService.hasProtectionFromSource(
                                        gameData, permanent, creature.getCard(), attackerId))
                                .map(Permanent::getId)
                                .toList();
                        if (creatureTargetIds.isEmpty()) {
                            gameLogService.append(gameData, GameLog.cardThen(creature.getCard(),
                                    "'s ability does not trigger — " + gameData.playerIdToName.get(defenderId) + " has no creatures."));
                            continue;
                        }
                        if (may.wrapped() instanceof SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect sacrifice) {
                            queuedMay = new MayEffect(
                                    new SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect(
                                            sacrifice.cannotBeRegenerated(), creatureTargetIds),
                                    may.prompt(), may.elseEffect(), may.choicePlayer());
                        }
                    }
                    if (may.wrapped() instanceof ExilePermanentDamagedPlayerControlsEffect exileEffect) {
                        List<Permanent> defenderBf = gameData.playerBattlefields.get(defenderId);
                        boolean hasValidTargets = false;
                        if (defenderBf != null) {
                            for (Permanent p : defenderBf) {
                                if (exileEffect.predicate() == null
                                        || predicateEvaluationService.matchesPermanentPredicate(gameData, p, exileEffect.predicate())) {
                                    hasValidTargets = true;
                                    break;
                                }
                            }
                        }
                        if (!hasValidTargets) {
                            gameLogService.append(gameData, GameLog.cardThen(creature.getCard(),
                                    "'s ability does not trigger — " + gameData.playerIdToName.get(defenderId) + " has no valid targets."));
                            continue;
                        }
                    }
                    if (may.wrapped() instanceof DamagedPlayerControlsTargetEffect damageEffect) {
                        if (damageDealt >= damageEffect.minimumDamage()) {
                            gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                                    creature.getCard(), attackerId,
                                    List.of(new MayEffect(damageEffect.forDamagedPlayer(defenderId, damageDealt),
                                            may.prompt(), may.elseEffect(), may.choicePlayer())),
                                    creature.getId(), damageEffect.targetChooserId(defenderId, attackerId), defenderId));
                        }
                        continue;
                    }
                    // Wire the combat damage dealt as the event value for dynamic combat-damage
                    // amounts, such as "draw that many cards" or "deal that much damage".
                    int mayEventValue = readsCombatDamage(may.wrapped()) ? damageDealt : 0;
                    // A "target creature" (any) may-ability — e.g. Hapatra's "you may put a -1/-1 counter
                    // on target creature" — is queued with a null target so its target is chosen at
                    // resolution. Effects that instead act on a creature the damaged player controls have
                    // a NONE target spec and keep the baked-in defender context.
                    UUID mayTargetId = may.wrapped().targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                            ? null : defenderId;
                    gameData.queueMayAbility(creature.getCard(), attackerId, queuedMay, mayTargetId, creature.getId(), mayEventValue);
                    gameLogService.append(gameData, GameLog.cardThen(creature.getCard(), "'s combat damage trigger fires."));
                    continue;
                }

                CardEffect specialEffect = effect instanceof ConditionalEffect conditional
                        ? conditional.wrapped() : effect;
                if (specialEffect instanceof TapChosenPermanentEffect tap
                        && tap.chooseFromDamagedPlayer()) {
                    CardEffect targeted = tap.forDamagedPlayer(defenderId);
                    if (effect instanceof ConditionalEffect conditional) {
                        targeted = new ConditionalEffect(conditional.condition(), targeted, conditional.interveningIf());
                    }
                    gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                            creature.getCard(), attackerId, List.of(targeted),
                            creature.getId(), attackerId, defenderId));
                    continue;
                }
                if (specialEffect instanceof DamagedPlayerControlsTargetEffect damageEffect) {
                    if (damageDealt >= damageEffect.minimumDamage()) {
                        CardEffect targeted = damageEffect.forDamagedPlayer(defenderId, damageDealt);
                        if (effect instanceof ConditionalEffect conditional) {
                            targeted = new ConditionalEffect(conditional.condition(), targeted, conditional.interveningIf());
                        }
                        gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                                creature.getCard(), attackerId, List.of(targeted), creature.getId(),
                                damageEffect.targetChooserId(defenderId, attackerId), defenderId));
                    }
                    continue;
                }
                if (specialEffect instanceof ExilePermanentDamagedPlayerControlsEffect exileEffect) {
                    List<Permanent> defenderBf = gameData.playerBattlefields.get(defenderId);
                    boolean hasValidTargets = defenderBf != null && defenderBf.stream()
                            .anyMatch(permanent -> exileEffect.predicate() == null
                                    || predicateEvaluationService.matchesPermanentPredicate(
                                    gameData, permanent, exileEffect.predicate()));
                    if (!hasValidTargets) {
                        gameLogService.append(gameData, GameLog.cardThen(creature.getCard(),
                                "'s ability does not trigger \u2014 " + gameData.playerIdToName.get(defenderId)
                                        + " has no valid targets."));
                        continue;
                    }
                    StackEntry exileEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(),
                            attackerId, creature.getCard().getName() + "'s triggered ability", List.of(effect),
                            defenderId, creature.getId());
                    exileEntry.setNonTargeting(true);
                    gameData.stack.add(exileEntry);
                    gameLogService.append(gameData, GameLog.cardThen(creature.getCard(),
                            "'s combat damage trigger goes on the stack."));
                    continue;
                }

                if (effect instanceof SacrificePermanentDamagedPlayerControlsEffect sacrificeEffect) {
                    if (damageDealt < sacrificeEffect.minimumDamage()) {
                        gameLogService.append(gameData, GameLog.cardThen(creature.getCard(),
                                "'s ability does not trigger — less than " + sacrificeEffect.minimumDamage()
                                + " damage dealt."));
                        continue;
                    }
                    List<Permanent> defenderBf = gameData.playerBattlefields.get(defenderId);
                    boolean hasValidTargets = false;
                    if (defenderBf != null) {
                        for (Permanent p : defenderBf) {
                            if (sacrificeEffect.predicate() == null
                                    || predicateEvaluationService.matchesPermanentPredicate(gameData, p, sacrificeEffect.predicate())) {
                                hasValidTargets = true;
                                break;
                            }
                        }
                    }
                    if (!hasValidTargets) {
                        gameLogService.append(gameData, GameLog.cardThen(creature.getCard(),
                                "'s ability does not trigger — " + gameData.playerIdToName.get(defenderId)
                                + " has no valid targets."));
                        continue;
                    }
                    StackEntry sacrificeSe = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            creature.getCard().getName() + "'s triggered ability", List.of(effect), defenderId, creature.getId());
                    sacrificeSe.setNonTargeting(true);
                    gameData.stack.add(sacrificeSe);
                    gameLogService.append(gameData, GameLog.cardThen(creature.getCard(), "'s combat damage trigger goes on the stack."));
                    continue;
                }

                // "exile up to N target cards from that player's graveyard" (Skullsnatcher): the
                // targets are chosen from the damaged player's graveyard as the trigger goes on the
                // stack, so the targeting service owns pushing the entry (and its own logging).
                if (effect instanceof GraveyardCardChoosingEffect graveyardChoice
                        && graveyardChoice.choosesGraveyardCards()) {
                    graveyardTargetingService.handleCombatDamageGraveyardChoiceTargeting(gameData, attackerId,
                            creature.getCard(), List.of(effect), creature.getId(), defenderId, graveyardChoice);
                    continue;
                }

                // Graveyard-targeting return ("return target card from your graveyard to your hand",
                // e.g. Charnelhoard Wurm): choose the graveyard target as the trigger goes on the
                // stack via the shared SpellGraveyardTargetTrigger flow (drained by AutoPassService).
                // The trigger path allows an empty selection, so "you may return target" reads as
                // up-to-one (decline = choose 0) with no MayEffect wrapper.
                if (effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                    // "from that player's graveyard" (Ink-Eyes): an opponent-graveyard scope on a
                    // combat damage trigger means the damaged player's graveyard specifically, so the
                    // choice is narrowed to them rather than every opponent.
                    UUID graveyardOwnerId =
                            effect.targetSpec().graveyardScope().orElse(null) == GraveyardSearchScope.OPPONENT_GRAVEYARD
                                    ? defenderId : null;
                    gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                            creature.getCard(), attackerId, new ArrayList<>(List.of(effect)), graveyardOwnerId, 0, 0,
                            creature.isAlternateCost()));
                    gameLogService.append(gameData, GameLog.text(creature.getCard().getName() + "'s combat damage trigger fires."));
                    continue;
                }

                boolean targetsExiledCard = effect.targetSpec().admits(TargetPredicate.Kind.EXILED_CARD);
                if ((effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                        || targetsExiledCard)
                        && (targetsExiledCard
                        || !(effect instanceof CombatDamageTriggerContextEffect contextEffect)
                        || contextEffect.combatDamageTriggerContext() == null
                        || creature.getCard().hasEffectTargetIndex(effect))
                        && !(effect instanceof CombatOpponentReferencingEffect c && c.referencesCombatOpponent())) {
                    if (triggerCollectionService.needsSlotBySlotTargetSelection(creature.getCard())) {
                        gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                                creature.getCard(), attackerId, List.of(effect), creature.getId(), List.of(), 0, 0));
                    } else {
                        gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                                creature.getCard(), attackerId, List.of(effect), creature.getId(), attackerId, defenderId));
                    }
                    gameLogService.append(gameData, GameLog.cardThen(creature.getCard(),
                            "'s combat damage trigger goes on the stack — choose a target."));
                    continue;
                }

                String desc = creature.getCard().getName() + "'s triggered ability";
                StackEntry se;
                // A single capability interface reports the context the fired trigger needs (which
                // player it targets, which permanent is its source); a null context means the plain
                // stack entry (no bound player or source). Recipient-dependent effects (discard /
                // mill / damage to the TARGET_PLAYER) report DAMAGED_PLAYER only for that recipient
                // and null otherwise, matching the old per-recipient guards.
                CombatDamageTriggerContextEffect.TriggerContext triggerContext =
                        effect instanceof CombatDamageTriggerContextEffect contextEffect
                                ? contextEffect.combatDamageTriggerContext()
                                : null;
                if (triggerContext == CombatDamageTriggerContextEffect.TriggerContext.DAMAGED_PLAYER_WITH_DAMAGE_AMOUNT) {
                    se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            desc, List.of(effect), defenderId, creature.getId());
                    se.setXValue(damageDealt);
                } else if (triggerContext == CombatDamageTriggerContextEffect.TriggerContext.SOURCE_SELF) {
                    se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            desc, List.of(effect), null, creature.getId());
                } else if (triggerContext == CombatDamageTriggerContextEffect.TriggerContext.DAMAGED_PLAYER) {
                    se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            desc, List.of(effect), defenderId, creature.getId());
                } else if (triggerContext == CombatDamageTriggerContextEffect.TriggerContext.CHOSEN_PLAYER) {
                    se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            desc, List.of(effect), creature.getRememberedTargetPlayerId(), creature.getId());
                } else {
                    se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            desc, List.of(effect), null, creature.getId());
                }
                if (se.getSourcePermanentId() != null) {
                    se.setSourcePermanentSnapshot(new Permanent(creature));
                    se.setAlternateCost(creature.isAlternateCost());
                }
                // Wire the combat damage dealt as the event value so "discards that many cards"
                // (DiscardEffect with an EventValue amount, e.g. Needle Specter) or "draw that many
                // cards" (DrawCardEffect with an EventValue amount, e.g. Hunter's Insight) or "mills
                // that many cards" (MillEffect with an EventValue amount, e.g. Crosstown Courier)
                // reads it.
                setCombatDamageEventValue(se, effect, damageDealt);
                se.setNonTargeting(true);
                gameData.stack.add(se);
                gameLogService.append(gameData, GameLog.cardThen(creature.getCard(), "'s combat damage trigger goes on the stack."));
            }
            } finally {
                gameData.restoreTriggeredAbilityCopies(previousCopies);
            }

            if (creature.isHasDamageToOpponentCreatureBounce()) {
                int bouncePreviousCopies = beginCombatDamageTriggerCopies(
                        gameData, attackerId, creature, creature, attackerId, damageDealt);
                try {
                String desc = creature.getCard().getName() + "'s triggered ability";
                StackEntry bounceSe = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                        desc, List.of(new ReturnPermanentsOnCombatDamageToPlayerEffect(new PermanentIsCreaturePredicate())), 1, defenderId, null);
                bounceSe.setNonTargeting(true);
                gameData.stack.add(bounceSe);
                gameLogService.append(gameData, GameLog.cardThen(creature.getCard(), "'s damage-to-opponent bounce trigger goes on the stack."));
                } finally {
                    gameData.restoreTriggeredAbilityCopies(bouncePreviousCopies);
                }
            }

            checkAttachedCombatDamageToPlayerTriggers(gameData, creature, attackerId, defenderId, damageDealt,
                    attachedDamageSources);
            checkPlayerAttachedCurseCombatDamageTriggers(gameData, creature, attackerId, defenderId, damageDealt);
            checkAllyCreatureCombatDamageToPlayerTriggers(gameData, creature, attackerId, defenderId, damageDealt,
                    combatDamageDealtToPlayer, firedBatchedAllyTriggerSources, false, false,
                    departedWatchersByController.getOrDefault(attackerId, List.of()));
            for (UUID otherPlayerId : gameData.orderedPlayerIds) {
                if (otherPlayerId.equals(attackerId)) continue;
                checkAllyCreatureCombatDamageToPlayerTriggers(gameData, creature, otherPlayerId, defenderId,
                        damageDealt, combatDamageDealtToPlayer, firedBatchedAllyTriggerSources, false, true,
                        departedWatchersByController.getOrDefault(otherPlayerId, List.of()));
            }
            checkSameNameCreatureCombatDamageToPlayerTriggers(
                    gameData, creature, attackerId, defenderId, damageDealt);
            triggerCollectionService.checkPlanarAllyCreatureCombatDamageToPlayerTriggers(
                    gameData, creature, attackerId, defenderId, damageDealt, firedBatchedAllyTriggerSources);
            triggerCollectionService.checkAnyCreatureCombatDamageToOpponentTriggers(
                    gameData, creature, attackerId, defenderId, damageDealt);
            triggerCollectionService.checkAnyCreatureCombatDamageToOwnerTriggers(
                    gameData, creature, attackerId, defenderId, damageDealt);
        }
    }

    /**
     * Checks global triggers that watch for a creature with the same name as the watcher dealing
     * combat damage to a player. The watcher need not be controlled by the creature's controller;
     * this models text such as Pirated Copy's "this creature or another creature with the same name".
     */
    private void checkSameNameCreatureCombatDamageToPlayerTriggers(GameData gameData, Permanent creature,
                                                                    UUID attackerId,
                                                                    UUID defenderId,
                                                                    int damageDealt) {
        gameData.forEachPermanent((watcherControllerId, watcher) -> {
            List<CardEffect> effects = new ArrayList<>(watcher.getCard().getEffects(
                    EffectSlot.ON_CREATURE_WITH_SAME_NAME_COMBAT_DAMAGE_TO_PLAYER));
            effects.addAll(watcher.getTemporaryTriggeredEffects(
                    EffectSlot.ON_CREATURE_WITH_SAME_NAME_COMBAT_DAMAGE_TO_PLAYER));
            effects.addAll(watcher.getPersistentTriggeredEffects(
                    EffectSlot.ON_CREATURE_WITH_SAME_NAME_COMBAT_DAMAGE_TO_PLAYER));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, watcher, EffectSlot.ON_CREATURE_WITH_SAME_NAME_COMBAT_DAMAGE_TO_PLAYER));

            FilterContext triggerContext = FilterContext.of(gameData)
                    .withSourceCardId(watcher.getCard().getId())
                    .withSourceControllerId(watcherControllerId)
                    .withSourcePermanentId(watcher.getId());
            for (CardEffect authoredEffect : effects) {
                CardEffect effect = OncePerTurnTriggerSupport.unwrapIfAvailable(gameData, watcher, authoredEffect);
                if (!(effect instanceof AllyCombatDamageTriggerEffect trigger)) {
                    continue;
                }
                if (trigger.dealerPredicate() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(
                        creature, trigger.dealerPredicate(), triggerContext)) {
                    continue;
                }

                CardEffect firedEffect = trigger.effect() instanceof CombatDamageAmountAwareEffect amountAware
                        ? amountAware.snapshotCombatDamage(damageDealt) : trigger.effect();
                StackEntry stackEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        watcher.getCard(),
                        watcherControllerId,
                        watcher.getCard().getName() + "'s triggered ability",
                        List.of(firedEffect),
                        defenderId,
                        watcher.getId());
                setCombatDamageEventValue(stackEntry, firedEffect, damageDealt);
                stackEntry.setNonTargeting(true);
                int previousCopies = beginCombatDamageTriggerCopies(
                        gameData, watcherControllerId, watcher, creature,
                        attackerId, damageDealt);
                try {
                    gameData.stack.add(stackEntry);
                    gameLogService.append(gameData, GameLog.cardThen(watcher.getCard(),
                            "'s same-name combat damage trigger goes on the stack."));
                } finally {
                    gameData.restoreTriggeredAbilityCopies(previousCopies);
                }
            }
        });
    }

    private void checkAttachedCombatDamageToPlayerTriggers(GameData gameData, Permanent creature, UUID attackerId,
                                                           UUID defenderId, int damageDealt,
                                                           Map<Permanent, UUID> attachedDamageSources) {
        attachedDamageSources.forEach((perm, ownerId) -> {
            if (perm.isAttached() && perm.getAttachedTo().equals(creature.getId())
                    && !gameQueryService.hasLostPrintedAbilities(gameData, perm)) {
                List<CardEffect> rawEffects = new ArrayList<>();
                if (perm.getCard().getEffects(
                        EffectSlot.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLAYER).isEmpty()) {
                    rawEffects.addAll(perm.getCard().getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER));
                }
                rawEffects.addAll(perm.getCard().getEffects(EffectSlot.ON_DAMAGE_TO_PLAYER));
                rawEffects.addAll(perm.getTemporaryTriggeredEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER));
                rawEffects.addAll(perm.getTemporaryTriggeredEffects(EffectSlot.ON_DAMAGE_TO_PLAYER));
                rawEffects.addAll(perm.getPersistentTriggeredEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER));
                rawEffects.addAll(perm.getPersistentTriggeredEffects(EffectSlot.ON_DAMAGE_TO_PLAYER));
                if (!ownerId.equals(defenderId)) {
                    rawEffects.addAll(perm.getCard().getEffects(EffectSlot.ON_DAMAGE_TO_OPPONENT));
                    rawEffects.addAll(perm.getTemporaryTriggeredEffects(EffectSlot.ON_DAMAGE_TO_OPPONENT));
                    rawEffects.addAll(perm.getPersistentTriggeredEffects(EffectSlot.ON_DAMAGE_TO_OPPONENT));
                }

                // Unwrap EnchantedPermanentConditionalEffect against the enchanted creature that
                // dealt the damage, so a granted trigger gated on the creature's characteristics
                // (e.g. Helm of the Ghastlord's colour-conditional draw/discard) only fires on the
                // matching branch. Null branches contribute nothing.
                List<CardEffect> effects = new ArrayList<>();
                for (CardEffect effect : rawEffects) {
                    if (effect instanceof com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfCombatDamageSourceIntoHandEffect
                            && creature.getCard().isToken()) {
                        continue;
                    }
                    if (effect instanceof EnchantedPermanentConditionalEffect cond) {
                        CardEffect active = predicateEvaluationService.matchesPermanentPredicate(gameData, creature, cond.filter())
                                ? cond.ifMatch()
                                : cond.ifNotMatch();
                        if (active != null) {
                            effects.add(active);
                        }
                    } else if ((effect instanceof MayEffect may ? may.wrapped() : effect)
                            instanceof DamagedPlayerControlsTargetEffect damagedPlayerEffect) {
                        if (damageDealt >= damagedPlayerEffect.minimumDamage()) {
                            CardEffect targeted = damagedPlayerEffect.forDamagedPlayer(defenderId);
                            effects.add(effect instanceof MayEffect may
                                    ? new MayEffect(targeted, may.prompt(), may.elseEffect(), may.choicePlayer())
                                    : targeted);
                        }
                    } else {
                        effects.add(effect);
                    }
                }

                int previousCopies = beginCombatDamageTriggerCopies(
                        gameData, ownerId, perm, creature, attackerId, damageDealt);
                try {
                if (!effects.isEmpty()) {
                    boolean needsGraveyardTarget = effects.stream()
                            .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
                    if (needsGraveyardTarget) {
                        gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                                perm.getCard(), ownerId, new ArrayList<>(effects), null));
                        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(),
                                "'s combat damage trigger goes on the stack — choose a graveyard target."));
                        return;
                    }

                    UUID triggerTargetId = defenderId;
                    boolean targetsEnchantedCreatureController = effects.stream()
                            .anyMatch(effect -> effect instanceof CombatDamageTriggerContextEffect contextEffect
                                    && contextEffect.combatDamageTriggerContext()
                                    == CombatDamageTriggerContextEffect.TriggerContext.ENCHANTED_CREATURE_CONTROLLER
                                    || effect instanceof CreateTokenForTriggeringPlayerEffect);
                    if (targetsEnchantedCreatureController) {
                        triggerTargetId = gameData.findControllerOf(creature);
                    }

                    boolean needsTarget = effects.stream()
                            .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
                    if (needsTarget) {
                        if (triggerCollectionService.needsSlotBySlotTargetSelection(perm.getCard())) {
                            gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                                    perm.getCard(), ownerId, effects, perm.getId(), List.of(), 0, 0));
                        } else {
                            gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                                    perm.getCard(), ownerId, effects, perm.getId(), ownerId, defenderId));
                        }
                        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(),
                                "'s combat damage trigger goes on the stack — choose a target."));
                    } else {
                        UUID sourcePermanentId = perm.getId();
                        // The Equipment itself deals the damage (Sword of War and Peace), so it keeps
                        // its own characteristics rather than the equipped creature's infect or lifelink.
                        boolean equipmentDealsDamage = effects.stream()
                                .anyMatch(com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect.class::isInstance);
                        boolean bindsToDamageDealer = !equipmentDealsDamage && effects.stream()
                                .anyMatch(effect -> effect instanceof CombatDamageTriggerContextEffect contextEffect
                                        && (contextEffect.combatDamageTriggerContext()
                                        == CombatDamageTriggerContextEffect.TriggerContext.SOURCE_SELF
                                        || contextEffect.combatDamageTriggerContext()
                                        == CombatDamageTriggerContextEffect.TriggerContext.DAMAGED_PLAYER));
                        if (bindsToDamageDealer) {
                            sourcePermanentId = creature.getId();
                        }
                        StackEntry se = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                ownerId,
                                perm.getCard().getName() + "'s triggered ability",
                                new ArrayList<>(effects),
                                triggerTargetId,
                                sourcePermanentId
                        );
                        if (creature.getId().equals(sourcePermanentId)) {
                            se.setSourcePermanentSnapshot(new Permanent(creature));
                        } else if (perm.getId().equals(sourcePermanentId)) {
                            se.setSourcePermanentSnapshot(new Permanent(perm));
                        }
                        for (CardEffect effect : effects) {
                            setCombatDamageEventValue(se, effect, damageDealt);
                        }
                        se.setNonTargeting(true);
                        gameData.stack.add(se);
                        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), "'s combat damage trigger goes on the stack."));
                    }
                }
                } finally {
                    gameData.restoreTriggeredAbilityCopies(previousCopies);
                }
            }
        });
    }

    private int beginCombatDamageTriggerCopies(GameData gameData, UUID abilityControllerId,
                                                Permanent abilitySource, Permanent damageSource,
                                                UUID damageSourceControllerId, int damageToPlayers) {
        if (abilityControllerId == null || abilitySource == null || damageSource == null) {
            return gameData.beginTriggeredAbilityCopies(1);
        }
        TriggerContext.SourceDealsCombatDamage context = new TriggerContext.SourceDealsCombatDamage(
                damageSource.getCard(), damageSourceControllerId, damageSource.getId(),
                damageToPlayers, damageToPlayers);
        return gameData.beginTriggeredAbilityCopies(1
                + gameQueryService.countAdditionalTriggeredAbilityTriggers(
                gameData, abilityControllerId, abilitySource, false, context));
    }

    private void setCombatDamageEventValue(StackEntry entry, CardEffect effect, int damageDealt) {
        if (effect instanceof SequenceEffect sequence) {
            for (CardEffect step : sequence.steps()) {
                setCombatDamageEventValue(entry, step, damageDealt);
            }
            return;
        }
        if (effect instanceof ConditionalEffect conditional) {
            setCombatDamageEventValue(entry, conditional.wrapped(), damageDealt);
            return;
        }
        if (effect instanceof MayEffect may) {
            setCombatDamageEventValue(entry, may.wrapped(), damageDealt);
            if (may.elseEffect() != null) {
                setCombatDamageEventValue(entry, may.elseEffect(), damageDealt);
            }
            return;
        }
        if (readsCombatDamage(effect)) {
            entry.setEventValue(damageDealt);
        }
    }

    private boolean readsCombatDamage(CardEffect effect) {
        if (effect instanceof SequenceEffect sequence) {
            return sequence.steps().stream().anyMatch(this::readsCombatDamage);
        }
        return effect instanceof DiscardEffect
                || (effect instanceof com.github.laxika.magicalvibes.model.effect.DiscoverEffect discover
                        && discover.discoverValue() instanceof EventValue)
                || (effect instanceof DrawCardEffect draw && draw.amount() instanceof EventValue)
                || (effect instanceof MillEffect mill && mill.count() instanceof EventValue)
                || (effect instanceof CombatDamageAmountAwareEffect amountAware
                && amountEvaluationService.referencesEventValue(amountAware.combatDamageAmount()));
    }

    /**
     * Checks for curse enchantments attached to the defending player that trigger on combat damage.
     * E.g. Curse of Stalked Prey: "Whenever a creature deals combat damage to enchanted player,
     * put a +1/+1 counter on that creature."
     */
    private void checkPlayerAttachedCurseCombatDamageTriggers(GameData gameData, Permanent creature, UUID attackerId,
                                                              UUID defenderId, int damageDealt) {
        gameData.forEachPermanent((ownerId, perm) -> {
            if (perm.isAttached() && defenderId.equals(perm.getAttachedTo())) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER);
                for (CardEffect effect : effects) {
                    if (effect instanceof CombatDamageTriggerContextEffect contextEffect
                            && (contextEffect.combatDamageTriggerContext()
                            == CombatDamageTriggerContextEffect.TriggerContext.SOURCE_SELF
                            || contextEffect.combatDamageTriggerContext()
                            == CombatDamageTriggerContextEffect.TriggerContext.DAMAGED_PLAYER)) {
                        CombatDamageTriggerContextEffect.TriggerContext triggerContext =
                                contextEffect.combatDamageTriggerContext();
                        UUID targetId = triggerContext
                                == CombatDamageTriggerContextEffect.TriggerContext.DAMAGED_PLAYER
                                ? defenderId : null;
                        UUID sourcePermanentId = triggerContext
                                == CombatDamageTriggerContextEffect.TriggerContext.SOURCE_SELF
                                ? creature.getId() : perm.getId();
                        int previousCopies = beginCombatDamageTriggerCopies(
                                gameData, ownerId, perm, creature, attackerId, damageDealt);
                        try {
                        StackEntry se = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                ownerId,
                                perm.getCard().getName() + "'s triggered ability",
                                List.of(effect),
                                targetId,
                                sourcePermanentId
                        );
                        se.setTriggeringPermanentId(creature.getId());
                        se.setTriggeringPermanentControllerId(attackerId);
                        setCombatDamageEventValue(se, effect, damageDealt);
                        se.setNonTargeting(true);
                        gameData.stack.add(se);
                        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(),
                                "'s combat damage trigger goes on the stack."));
                        } finally {
                            gameData.restoreTriggeredAbilityCopies(previousCopies);
                        }
                    }
                }
            }
        });
    }

    private static boolean isOpponentDealtDamageTrigger(CardEffect effect) {
        return effect instanceof AllyCombatDamageTriggerEffect trigger && trigger.opponentDealtDamage();
    }

    private static boolean isOwnerRelativeCommanderTrigger(CardEffect effect) {
        return effect instanceof AllyCombatDamageTriggerEffect trigger
                && trigger.dealerPredicate() instanceof PermanentIsCommanderPredicate commander
                && commander.ownedBySourceController();
    }

    /**
     * Checks for permanents the attacker controls that trigger when an ally creature deals
     * combat damage to a player. E.g. Rakish Heir: "Whenever a Vampire you control deals
     * combat damage to a player, put a +1/+1 counter on it."
     *
     * <p>When {@code ownerRelativePass} is set, {@code attackerId} is a player other than the
     * dealer's controller and only abilities keyed to a commander that player owns ("your
     * commander", e.g. Jocasta, Automaton Avenger) are considered, since those still trigger while
     * another player controls the commander. The dealer predicate decides whether the dealer
     * qualifies for that player.
     */
    private void checkAllyCreatureCombatDamageToPlayerTriggers(GameData gameData, Permanent creature, UUID attackerId,
                                                               UUID defenderId, int damageDealt,
                                                               Map<Permanent, Integer> combatDamageDealtToPlayer,
                                                               Set<UUID> firedBatchedAllyTriggerSources,
                                                               boolean battleDamage, boolean ownerRelativePass) {
        checkAllyCreatureCombatDamageToPlayerTriggers(gameData, creature, attackerId, defenderId, damageDealt,
                combatDamageDealtToPlayer, firedBatchedAllyTriggerSources, battleDamage, ownerRelativePass,
                List.of());
    }

    /**
     * {@code departedWatchers} are permanents that were on the battlefield when the combat damage was
     * dealt but were put into a graveyard by state-based actions before these triggers were collected;
     * they still trigger off the damage dealt at the same time.
     */
    private void checkAllyCreatureCombatDamageToPlayerTriggers(GameData gameData, Permanent creature, UUID attackerId,
                                                               UUID defenderId, int damageDealt,
                                                               Map<Permanent, Integer> combatDamageDealtToPlayer,
                                                               Set<UUID> firedBatchedAllyTriggerSources,
                                                               boolean battleDamage, boolean ownerRelativePass,
                                                               List<Permanent> departedWatchers) {
        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(attackerId);
        if (!battleDamage && !ownerRelativePass) {
            triggerCollectionService.checkEmblemAllyCreatureCombatDamageToPlayerTriggers(
                    gameData, creature, attackerId, damageDealt);
            triggerCollectionService.collectTemporaryGlobalTriggers(
                    gameData, EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                    creature.getId(), damageDealt);
        }
        if (attackerBattlefield == null) return;

        List<Permanent> watchers = new ArrayList<>(attackerBattlefield);
        watchers.addAll(departedWatchers);
        for (Permanent perm : watchers) {
            if (perm.isFaceDown() || gameQueryService.computeStaticBonus(gameData, perm).losesAllAbilities()
                    || perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
            List<CardEffect> effects = new ArrayList<>();
            if (!battleDamage) {
                effects.addAll(perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER));
                effects.addAll(perm.getTemporaryTriggeredEffects(
                        EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER));
                effects.addAll(perm.getPersistentTriggeredEffects(
                        EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER));
                effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                        gameData, perm, EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER));
            }
            effects.addAll(perm.getCard().getEffects(
                    EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE));
            effects.addAll(perm.getTemporaryTriggeredEffects(
                    EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE));
            effects.addAll(perm.getPersistentTriggeredEffects(
                    EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                    gameData, perm, EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE));
            int previousCopies = beginCombatDamageTriggerCopies(
                    gameData, attackerId, perm, creature, attackerId, damageDealt);
            try {
            for (CardEffect authoredEffect : effects) {
                CardEffect effect = OncePerTurnTriggerSupport.unwrapIfAvailable(gameData, perm, authoredEffect);
                if (ownerRelativePass && !isOwnerRelativeCommanderTrigger(effect)
                        && !isOpponentDealtDamageTrigger(effect)) {
                    continue;
                }
                if (effect instanceof AllyCombatDamageTriggerEffect trigger) {
                    if (trigger.opponentDealtDamage() && attackerId.equals(defenderId)) {
                        continue;
                    }
                    FilterContext triggerContext = FilterContext.of(gameData)
                            .withSourceControllerId(attackerId)
                            .withSourcePermanentId(perm.getId());
                    if (trigger.dealerPredicate() != null
                            && !predicateEvaluationService.matchesPermanentPredicate(
                            creature, trigger.dealerPredicate(), triggerContext)) {
                        continue;
                    }
                    // "One or more ... deal combat damage" is a single event for the whole batch,
                    // so a batched trigger only goes on the stack for the first matching dealer.
                    if (trigger.oncePerDamageStep() && !firedBatchedAllyTriggerSources.add(perm.getId())) {
                        continue;
                    }
                    CardEffect firedEffect = trigger.effect();
                    if (firedEffect instanceof ConditionalEffect conditional
                            && (conditional.condition() instanceof SourceCounterThreshold
                            || conditional.condition() instanceof SourceHasChosenMode)) {
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forPermanent(perm, attackerId))) {
                            continue;
                        }
                        firedEffect = conditional.wrapped();
                    } else if (firedEffect instanceof ConditionalEffect conditional
                            && conditional.interveningIf()
                            && conditional.condition() instanceof NoMonarch
                            && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                            ConditionContext.forPermanent(perm, attackerId))) {
                        continue;
                    }
                    if (trigger.oncePerDamageStep() && firedEffect instanceof CombatDamageDealerAwareEffect aware) {
                        List<UUID> dealerIds = combatDamageDealtToPlayer.entrySet().stream()
                                .filter(dealer -> dealer.getValue() > 0
                                        && (trigger.dealerPredicate() == null
                                        || predicateEvaluationService.matchesPermanentPredicate(
                                        dealer.getKey(), trigger.dealerPredicate(), triggerContext)))
                                .map(dealer -> dealer.getKey().getId())
                                .toList();
                        firedEffect = aware.withCombatDamageDealerIds(dealerIds);
                    }
                    int triggerDamage = trigger.oncePerDamageStep()
                            ? combatDamageDealtToPlayer.entrySet().stream()
                            .filter(dealer -> dealer.getValue() > 0
                                    && (trigger.dealerPredicate() == null
                                    || predicateEvaluationService.matchesPermanentPredicate(
                                    dealer.getKey(), trigger.dealerPredicate(), triggerContext)))
                            .mapToInt(Map.Entry::getValue)
                            .sum()
                            : damageDealt;
                    if (firedEffect instanceof CombatDamageAmountAwareEffect amountAware) {
                        firedEffect = amountAware.snapshotCombatDamage(triggerDamage);
                    }
                    if (firedEffect instanceof ChooseModeNotYetChosenThisTurnEffect modal) {
                        gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                                perm.getCard(), attackerId, new ChooseOneEffect(modal.options()),
                                perm.getId(), true, false, null, defenderId));
                        OncePerTurnTriggerSupport.markIfNeeded(gameData, perm, authoredEffect);
                        gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                        continue;
                    }
                    // Player recipients on this trigger slot normally mean "that player":
                    // bind the damaged player unless the card explicitly declares a target.
                    if (!(firedEffect instanceof CombatOpponentReferencingEffect opponentReference
                            && opponentReference.referencesCombatOpponent())
                            && (firedEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                            || (firedEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                            && (perm.getCard().hasEffectTargetIndex(authoredEffect)
                            || perm.getCard().hasEffectTargetIndex(firedEffect))))) {
                        gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                                perm.getCard(), attackerId, List.of(firedEffect), perm.getId(), attackerId, defenderId));
                        OncePerTurnTriggerSupport.markIfNeeded(gameData, perm, authoredEffect);
                        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(),
                                "'s combat damage trigger goes on the stack — choose a target."));
                        continue;
                    }
                    if (firedEffect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                        UUID graveyardOwnerId = firedEffect.targetSpec().graveyardScope().orElse(null)
                                == GraveyardSearchScope.OPPONENT_GRAVEYARD ? defenderId : null;
                        gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                                perm.getCard(), attackerId, new ArrayList<>(List.of(firedEffect)), graveyardOwnerId,
                                0, triggerDamage, 0, null, false, perm.getId()));
                        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(),
                                "'s combat damage trigger goes on the stack — choose a graveyard target."));
                        continue;
                    }
                    if ((firedEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                            || (firedEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                            && (perm.getCard().hasEffectTargetIndex(authoredEffect)
                            || perm.getCard().hasEffectTargetIndex(firedEffect))))
                            && (!(firedEffect instanceof CombatDamageTriggerContextEffect contextEffect)
                            || contextEffect.combatDamageTriggerContext() == null
                            || perm.getCard().hasEffectTargetIndex(firedEffect))
                            && !(firedEffect instanceof CombatOpponentReferencingEffect c
                            && c.referencesCombatOpponent())) {
                        gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                                perm.getCard(), attackerId, List.of(firedEffect), perm.getId(), attackerId,
                                defenderId, creature.getId()));
                        OncePerTurnTriggerSupport.markIfNeeded(gameData, perm, authoredEffect);
                        gameLogService.append(gameData, GameLog.cardThen(perm.getCard(),
                                "'s combat damage trigger goes on the stack — choose a target."));
                        continue;
                    }
                    // Bind the damaged player so effects like DiscardEffect(TARGET_PLAYER) resolve
                    // against them (Oona's Blackguard: "...that player discards a card").
                    UUID triggerTargetId = defenderId;
                    if (firedEffect instanceof CombatDamageTriggerContextEffect contextEffect
                            && contextEffect.combatDamageTriggerContext()
                            == CombatDamageTriggerContextEffect.TriggerContext.CHOSEN_PLAYER) {
                        triggerTargetId = perm.getRememberedTargetPlayerId();
                    }
                    StackEntry se = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            attackerId,
                            perm.getCard().getName() + "'s triggered ability",
                            List.of(firedEffect),
                            triggerTargetId,
                            trigger.bindSourceToDealer() ? creature.getId() : perm.getId()
                    );
                    // The combat damage dealt is this trigger's event value, so "put that many
                    // +1/+1 counters on it" (Necropolis Regent) can read it back at resolution.
                    se.setEventValue(triggerDamage);
                    se.setNonTargeting(true);
                    if (trigger.bindSourceToDealer()) {
                        // The dealer may have left the battlefield by resolution; keep its last known
                        // information so the ability still sees what it was (CR 608.2h).
                        se.setSourcePermanentSnapshot(new Permanent(creature));
                    }
                    if (authoredEffect instanceof OncePerTurnTriggerEffect once && once.markOnAcceptance()) {
                        se.setMarkSourceOncePerTurnOnAcceptance(true);
                    }
                    if (firedEffect instanceof CombatDamageDealerReferencingEffect) {
                        se.setTriggeringPermanentId(creature.getId());
                        se.setTriggeringPermanentControllerId(attackerId);
                        se.setDamageSourceCard(creature.getCard());
                        se.setSourcePermanentSnapshot(new Permanent(perm));
                    }
                    gameData.stack.add(se);
                    OncePerTurnTriggerSupport.markIfNeeded(gameData, perm, authoredEffect);
                    gameLogService.append(gameData, GameLog.cardThen(perm.getCard(),
                            "'s triggered ability goes on the stack."));
                }
            }
            } finally {
                gameData.restoreTriggeredAbilityCopies(previousCopies);
            }
        }

        // Graveyard-based variant (GRAVEYARD_ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER): the trigger
        // fires from the attacker's graveyard. E.g. Auntie's Snitch — "Whenever a Goblin or Rogue you
        // control deals combat damage to a player, if this card is in your graveyard, you may return
        // this card to your hand." The stack entry's source is the graveyard card itself.
        if (battleDamage) return;
        if (!ownerRelativePass) {
            checkGraveyardAllyCreatureCombatDamageTriggers(gameData, creature, attackerId,
                    EffectSlot.GRAVEYARD_ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER,
                    firedBatchedAllyTriggerSources);
        }
        List<Card> graveyard = gameData.playerGraveyards.get(attackerId);
        if (graveyard == null) return;
        for (Card card : new ArrayList<>(graveyard)) {
            for (CardEffect effect : gameQueryService.getEffectiveGraveyardEffects(
                    gameData, card, EffectSlot.GRAVEYARD_ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER)) {
                if (ownerRelativePass && !isOwnerRelativeCommanderTrigger(effect)) {
                    continue;
                }
                if (effect instanceof AllyCombatDamageTriggerEffect trigger) {
                    if (trigger.dealerPredicate() != null
                            && !predicateEvaluationService.matchesPermanentPredicate(gameData, creature, trigger.dealerPredicate())) {
                        continue;
                    }
                    if (trigger.oneOrMoreDealers() && !firedBatchedAllyTriggerSources.add(card.getId())) {
                        continue;
                    }
                    StackEntry se = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            card,
                            attackerId,
                            card.getName() + "'s graveyard trigger",
                            List.of(trigger.effect())
                    );
                    se.setTriggeringCardId(card.getId());
                    se.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
                    se.setNonTargeting(true);
                    gameData.stack.add(se);
                    gameLogService.append(gameData, GameLog.cardThen(card,
                            "'s graveyard trigger goes on the stack."));
                }
            }
        }
    }

    /**
     * Fires {@code GRAVEYARD_ON_COMBAT_DAMAGE_TO_YOU_OR_YOUR_PLANESWALKER} triggers from the
     * graveyards of every player who was dealt combat damage this step, either directly or on a
     * planeswalker they control. E.g. Vengeful Pharaoh — "Whenever combat damage is dealt to you or
     * a planeswalker you control, if this card is in your graveyard, destroy target attacking
     * creature, then put this card on top of your library."
     *
     * <p>The trigger targets, so it is routed through the {@code AttackTriggerTarget} pending-choice
     * pipeline rather than pushed straight onto the stack: the target is chosen as the ability is
     * put on the stack (CR 603.3d), and the ability is skipped entirely when no legal target exists.
     * The stack entry's source is the graveyard card itself (no source permanent).
     */
    private void checkGraveyardCombatDamageToYouOrPlaneswalkerTriggers(GameData gameData,
                                                                       CombatDamageState state,
                                                                       UUID defenderId) {
        Set<UUID> damagedPlayerIds = new LinkedHashSet<>();
        if (state.damageToDefendingPlayer > 0 || state.poisonDamageToDefendingPlayer > 0) {
            state.combatDamageDealtToPlayer.forEach((source, amount) -> {
                if (amount > 0) damagedPlayerIds.add(
                        state.combatDamagePlayerRecipients.getOrDefault(source, defenderId));
            });
        }
        for (var entry : state.damageToPlaneswalkers.entrySet()) {
            if (entry.getValue() <= 0) continue;
            UUID planeswalkerController = gameQueryService.findPermanentController(gameData, entry.getKey());
            if (planeswalkerController != null) {
                damagedPlayerIds.add(planeswalkerController);
            }
        }

        for (UUID playerId : damagedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : new ArrayList<>(graveyard)) {
                List<CardEffect> effects = gameQueryService.getEffectiveGraveyardEffects(
                        gameData, card, EffectSlot.GRAVEYARD_ON_COMBAT_DAMAGE_TO_YOU_OR_YOUR_PLANESWALKER);
                if (effects.isEmpty()) continue;
                gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                        card, playerId, new ArrayList<>(effects), null));
                gameLogService.append(gameData, GameLog.cardThen(card, "'s graveyard trigger triggers."));
            }
        }
    }

    /**
     * Checks delayed combat damage loot triggers registered this turn (e.g. Jace, Cunning Castaway's +1).
     * Each fires once per combat damage step if one or more creatures the controller controls dealt
     * combat damage to a player.
     */
    private void processDelayedCombatDamageLootTriggers(GameData gameData,
                                                         Map<Permanent, Integer> combatDamageDealtToPlayer,
                                                         UUID attackerId) {
        if (!gameData.hasDelayedAction(DelayedCombatDamageLoot.class)) return;

        // Check if any creature controlled by the trigger's controller dealt combat damage to a player
        for (DelayedCombatDamageLoot loot : gameData.getDelayedActions(DelayedCombatDamageLoot.class)) {
            boolean creatureDealtDamage = false;
            if (loot.controllerId().equals(attackerId)) {
                for (var dmgEntry : combatDamageDealtToPlayer.entrySet()) {
                    if (dmgEntry.getValue() > 0) {
                        creatureDealtDamage = true;
                        break;
                    }
                }
            }
            if (creatureDealtDamage) {
                StackEntry se = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        loot.sourceCard(),
                        loot.controllerId(),
                        loot.sourceCard().getName() + "'s delayed trigger",
                        List.of(new DrawCardEffect(loot.drawAmount()),
                                new DiscardEffect(loot.discardAmount(), DiscardRecipient.CONTROLLER))
                );
                se.setNonTargeting(true);
                gameData.stack.add(se);
                gameLogService.append(gameData, GameLog.cardThen(loot.sourceCard(),
                        "'s delayed trigger fires — draw " + loot.drawAmount()
                                + ", discard " + loot.discardAmount() + "."));
            }
        }
    }

    private void processDelayedCombatDamageTokenTriggers(GameData gameData,
                                                           Map<Permanent, Integer> combatDamageDealtToPlayer,
                                                           UUID attackerId,
                                                           UUID defenderId) {
        if (!gameData.hasDelayedAction(DelayedCombatDamageToken.class)) return;

        for (DelayedCombatDamageToken delayed : gameData.getDelayedActions(DelayedCombatDamageToken.class)) {
            if (!delayed.controllerId().equals(attackerId)
                    || !delayed.targetPlayerId().equals(defenderId)) {
                continue;
            }
            for (var damageEntry : combatDamageDealtToPlayer.entrySet()) {
                if (damageEntry.getValue() <= 0) continue;
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        delayed.sourceCard(),
                        delayed.controllerId(),
                        delayed.sourceCard().getName() + "'s delayed trigger",
                        List.of(delayed.tokenEffect())
                );
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);
                gameLogService.append(gameData, GameLog.cardThen(delayed.sourceCard(),
                        "'s delayed trigger goes on the stack."));
            }
        }
    }

    private void processDelayedCombatDamageBecomeMonarchTriggers(GameData gameData,
                                                                  CombatDamageState state,
                                                                  UUID activeId) {
        if (!gameData.hasDelayedAction(DelayedCombatDamageBecomeMonarch.class)) return;

        for (DelayedCombatDamageBecomeMonarch delayed
                : gameData.getDelayedActions(DelayedCombatDamageBecomeMonarch.class)) {
            boolean creatureDealtDamage = state.combatDamageDealtToPlayer.entrySet().stream()
                    .anyMatch(entry -> entry.getValue() > 0
                            && delayed.controllerId().equals(
                            state.combatDamageDealerControllers.getOrDefault(entry.getKey(), activeId)));
            if (!creatureDealtDamage) continue;

            StackEntry trigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    delayed.sourceCard(),
                    delayed.controllerId(),
                    delayed.sourceCard().getName() + "'s delayed trigger",
                    List.of(new BecomeMonarchEffect()));
            trigger.setNonTargeting(true);
            gameData.stack.add(trigger);
            gameLogService.append(gameData, GameLog.cardThen(delayed.sourceCard(),
                    "'s delayed trigger goes on the stack."));
        }
    }

    private void processDelayedCombatDamageDrawTriggers(GameData gameData, CombatDamageState state) {
        if (!gameData.hasDelayedAction(DelayedCombatDamageDraw.class)) return;

        Map<UUID, Permanent> damageSources = new LinkedHashMap<>();
        for (Permanent creature : state.combatDamageDealtToPlayer.keySet()) {
            damageSources.put(creature.getId(), creature);
        }
        for (Permanent creature : state.combatDamageDealtToPlaneswalker.keySet()) {
            damageSources.put(creature.getId(), creature);
        }

        for (CombatDamageState.DelayedCombatDamageDrawQualification qualification
                : state.delayedCombatDamageDrawQualifications) {
            DelayedCombatDamageDraw delayed = qualification.delayedAction();
            for (UUID sourceId : qualification.sourceIds()) {
                Permanent creature = damageSources.get(sourceId);
                if (creature == null) continue;
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        delayed.sourceCard(),
                        delayed.controllerId(),
                        delayed.sourceCard().getName() + "'s delayed trigger",
                        List.of(new DrawCardEffect(1)));
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);
                gameLogService.append(gameData, GameLog.cardThen(delayed.sourceCard(),
                        "'s delayed trigger fires — draw a card."));
            }
        }
    }

    private void processDelayedCombatDamageEffects(GameData gameData, CombatDamageState state) {
        if (state.combatDamageDealtToPlayer.isEmpty()
                || !gameData.hasDelayedAction(DelayedCombatDamageEffect.class)) {
            return;
        }

        for (DelayedCombatDamageEffect delayed
                : gameData.getDelayedActions(DelayedCombatDamageEffect.class)) {
            for (var entry : state.combatDamageDealtToPlayer.entrySet()) {
                Permanent creature = entry.getKey();
                if (entry.getValue() <= 0) continue;

                UUID controllerId = state.combatDamageDealerControllers.get(creature);
                if (controllerId == null) controllerId = gameData.findControllerOf(creature);
                if (!delayed.controllerId().equals(controllerId)) continue;

                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        delayed.sourceCard(),
                        delayed.controllerId(),
                        delayed.sourceCard().getName() + "'s delayed trigger",
                        List.of(delayed.triggerEffect()),
                        (UUID) null,
                        delayed.sourcePermanentId());
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);
                gameLogService.append(gameData, GameLog.abilityTriggers(delayed.sourceCard()));
            }
        }
    }

    private void processDelayedCombatDamageLookAtHandAndDrawTriggers(GameData gameData,
                                                                       CombatDamageState state) {
        for (CombatDamageState.DelayedCombatDamageLookAtHandAndDrawQualification qualification
                : state.delayedCombatDamageLookAtHandAndDrawQualifications) {
            DelayedCombatDamageLookAtHandAndDraw delayed = qualification.delayedAction();
            StackEntry trigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    delayed.sourceCard(),
                    delayed.controllerId(),
                    delayed.sourceCard().getName() + "'s delayed trigger",
                    List.of(new LookAtHandEffect(), new DrawCardEffect(1)),
                    qualification.damagedPlayerId(),
                    (UUID) null
            );
            trigger.setNonTargeting(true);
            gameData.stack.add(trigger);
            gameLogService.append(gameData, GameLog.cardThen(delayed.sourceCard(),
                    "'s delayed trigger fires — look at "
                            + gameData.playerIdToName.get(qualification.damagedPlayerId())
                            + "'s hand and draw a card."));
        }
    }

    private void processSelfDealsCombatDamageToPlayerOrPlaneswalkerTriggers(GameData gameData,
                                                                             CombatDamageState state) {
        for (Permanent source : state.combatDamageDealt.keySet()) {
            int damageDealtToPlayerOrPlaneswalker = state.combatDamageDealtToPlayer.getOrDefault(source, 0)
                    + state.combatDamageDealtToPlaneswalker.getOrDefault(source, 0);
            if (damageDealtToPlayerOrPlaneswalker <= 0) continue;

            UUID controllerId = state.combatDamageDealerControllers.get(source);
            if (controllerId == null) controllerId = gameData.findControllerOf(source);
            if (controllerId == null) continue;
            triggerCollectionService.queueSourceDealsCombatDamageToPlayerOrPlaneswalkerTriggers(
                    gameData, source.getCard(), controllerId, source.getId(), damageDealtToPlayerOrPlaneswalker,
                    state.combatDamageDealtToPlayer.getOrDefault(source, 0),
                    state.selfDealsCombatDamageToPlayerOrPlaneswalkerEffects.get(source));
        }
    }

    private void processSelfDealsCombatDamageToPlayerOrBattleTriggers(GameData gameData,
                                                                       CombatDamageState state) {
        for (Permanent source : state.combatDamageDealt.keySet()) {
            int battleDamage = state.combatDamageAmountsToBattles
                    .getOrDefault(source, Map.of()).values().stream().mapToInt(Integer::intValue).sum();
            int totalDamage = state.combatDamageDealtToPlayer.getOrDefault(source, 0) + battleDamage;
            if (totalDamage <= 0) continue;

            UUID controllerId = state.combatDamageDealerControllers.get(source);
            if (controllerId == null) controllerId = gameData.findControllerOf(source);
            if (controllerId == null) continue;
            triggerCollectionService.queueSourceDealsCombatDamageToPlayerOrBattleTriggers(
                    gameData, source.getCard(), controllerId, source.getId(), totalDamage,
                    state.combatDamageDealtToPlayer.getOrDefault(source, 0),
                    state.selfDealsCombatDamageToPlayerOrBattleEffects.get(source));
        }
    }

    private void processCombatDamageToBattleTriggers(GameData gameData, CombatDamageState state) {
        Map<UUID, Map<Permanent, Integer>> damageByBattle = new LinkedHashMap<>();
        for (var sourceEntry : state.combatDamageAmountsToBattles.entrySet()) {
            for (var battleEntry : sourceEntry.getValue().entrySet()) {
                damageByBattle.computeIfAbsent(battleEntry.getKey(), ignored -> new LinkedHashMap<>())
                        .put(sourceEntry.getKey(), battleEntry.getValue());
            }
        }
        for (var battleEntry : damageByBattle.entrySet()) {
            Set<UUID> firedBatchedAllyTriggerSources = new HashSet<>();
            for (var sourceEntry : battleEntry.getValue().entrySet()) {
                UUID controllerId = state.combatDamageDealerControllers.get(sourceEntry.getKey());
                if (controllerId == null) controllerId = gameData.findControllerOf(sourceEntry.getKey());
                if (controllerId == null) continue;
                checkAllyCreatureCombatDamageToPlayerTriggers(
                        gameData, sourceEntry.getKey(), controllerId, battleEntry.getKey(),
                        sourceEntry.getValue(), battleEntry.getValue(), firedBatchedAllyTriggerSources, true, false);
            }
        }
    }

    private void snapshotDelayedCombatDamageDrawSources(GameData gameData, CombatDamageState state) {
        if (!gameData.hasDelayedAction(DelayedCombatDamageDraw.class)) return;

        Map<UUID, Permanent> damageSources = new LinkedHashMap<>();
        for (Permanent creature : state.combatDamageDealtToPlayer.keySet()) {
            damageSources.put(creature.getId(), creature);
        }
        for (Permanent creature : state.combatDamageDealtToPlaneswalker.keySet()) {
            damageSources.put(creature.getId(), creature);
        }

        for (DelayedCombatDamageDraw delayed : gameData.getDelayedActions(DelayedCombatDamageDraw.class)) {
            Set<UUID> qualifyingSources = new LinkedHashSet<>();
            for (Permanent creature : damageSources.values()) {
                int playerDamage = state.combatDamageDealtToPlayer.getOrDefault(creature, 0);
                int planeswalkerDamage = state.combatDamageDealtToPlaneswalker.getOrDefault(creature, 0);
                if (playerDamage <= 0 && (!delayed.includesPlaneswalkers() || planeswalkerDamage <= 0)) continue;

                UUID controllerId = state.combatDamageDealerControllers.get(creature);
                if (controllerId == null) controllerId = gameData.findControllerOf(creature);
                if (!delayed.controllerId().equals(controllerId)) continue;
                if (delayed.sourcePredicate() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(
                        gameData, creature, delayed.sourcePredicate())) continue;
                qualifyingSources.add(creature.getId());
            }
            state.delayedCombatDamageDrawQualifications.add(
                    new CombatDamageState.DelayedCombatDamageDrawQualification(delayed, qualifyingSources));
        }
    }

    private void snapshotDelayedCombatDamageLookAtHandAndDrawSources(GameData gameData,
                                                                       CombatDamageState state,
                                                                       UUID damagedPlayerId) {
        if (!gameData.hasDelayedAction(DelayedCombatDamageLookAtHandAndDraw.class)) return;

        for (DelayedCombatDamageLookAtHandAndDraw delayed
                : gameData.getDelayedActions(DelayedCombatDamageLookAtHandAndDraw.class)) {
            boolean qualifyingCreatureDealtDamage = false;
            for (var damageEntry : state.combatDamageDealtToPlayer.entrySet()) {
                Permanent creature = damageEntry.getKey();
                if (damageEntry.getValue() <= 0) continue;

                UUID controllerId = state.combatDamageDealerControllers.get(creature);
                if (controllerId == null) controllerId = gameData.findControllerOf(creature);
                if (!delayed.controllerId().equals(controllerId)) continue;
                if (delayed.sourcePredicate() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(
                        gameData, creature, delayed.sourcePredicate())) continue;
                qualifyingCreatureDealtDamage = true;
                break;
            }
            if (qualifyingCreatureDealtDamage) {
                state.delayedCombatDamageLookAtHandAndDrawQualifications.add(
                        new CombatDamageState.DelayedCombatDamageLookAtHandAndDrawQualification(
                                delayed, damagedPlayerId));
            }
        }
    }

    /**
     * Processes combat damage reflection triggers registered this turn (e.g. Harsh Justice).
     * For each attacking creature that dealt combat damage to a protected player this step, the
     * creature deals that much damage back to its controller (the active player). Each creature
     * reflects separately, with the attacking creature as the damage source.
     */
    private void processCombatDamageReflectionTriggers(GameData gameData,
                                                       Map<Permanent, Integer> combatDamageDealtToPlayer,
                                                       UUID attackerId, UUID defenderId) {
        if (!gameData.hasDelayedAction(DelayedCombatDamageReflection.class)) return;

        for (DelayedCombatDamageReflection reflection : gameData.getDelayedActions(DelayedCombatDamageReflection.class)) {
            // Combat damage to a player always goes to the defending player in this combat.
            if (!reflection.protectedPlayerId().equals(defenderId)) continue;
            for (var dmgEntry : combatDamageDealtToPlayer.entrySet()) {
                Permanent attacker = dmgEntry.getKey();
                int damage = dmgEntry.getValue();
                if (damage <= 0) continue;
                StackEntry se = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        attacker.getCard(),
                        reflection.protectedPlayerId(),
                        reflection.sourceCard().getName() + "'s reflected combat damage",
                        List.of(new DealDamageToPlayersEffect(damage, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER)),
                        attackerId,
                        attacker.getId());
                se.setNonTargeting(true);
                se.setTriggeringPermanentId(attacker.getId());
                se.setTriggeringPermanentControllerId(attackerId);
                gameData.stack.add(se);
                gameLogService.append(gameData, GameLog.cardTextCard(reflection.sourceCard(),
                        " reflects " + damage + " combat damage from ", attacker.getCard(),
                        " to " + gameData.playerIdToName.get(attackerId) + "."));
            }
        }
    }

    private void processCombatDamageToCreatureTriggers(GameData gameData,
                                                        Map<Permanent, List<UUID>> combatDamageDealtToCreatures,
                                                        Map<Permanent, UUID> combatDamageDealerControllers,
                                                        Map<UUID, UUID> combatDamageTargetControllers,
                                                        Map<Permanent, List<CardEffect>> damageToCreatureEffects,
                                                        CombatDamageState state,
                                                        Map<UUID, Permanent> damagedCreatureSnapshots) {
        Map<UUID, Integer> damageByCreature = new HashMap<>();
        for (Map<UUID, Integer> damageAmounts : state.combatDamageAmountsToCreatures.values()) {
            damageAmounts.forEach((creatureId, amount) -> damageByCreature.merge(creatureId, amount, Integer::sum));
        }

        for (var entry : combatDamageDealtToCreatures.entrySet()) {
            Permanent source = entry.getKey();
            UUID controllerId = combatDamageDealerControllers.get(source);
            if (controllerId == null) continue;

            List<CardEffect> effects = damageToCreatureEffects.getOrDefault(source, List.of());
            if (effects.isEmpty()) continue;

            for (UUID damagedCreatureId : entry.getValue()) {
                int excessDamage = combatExcessDamage(gameData, state, damagedCreatureId,
                        damagedCreatureSnapshots.get(damagedCreatureId),
                        damageByCreature.getOrDefault(damagedCreatureId, 0));
                for (CardEffect effect : effects) {
                    if (effect instanceof CombatExcessDamageAwareEffect && excessDamage <= 0) {
                        continue;
                    }
                    // Damaged creature is the fixed "that creature" — bake it as targetId, no choice.
                    StackEntry trigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            source.getCard(),
                            controllerId,
                            source.getCard().getName() + "'s triggered ability",
                            List.of(effect),
                            damagedCreatureId,
                            source.getId()
                    );
                    trigger.setSourcePermanentSnapshot(new Permanent(source));
                    trigger.setTriggeringPermanentId(damagedCreatureId);
                    trigger.setTriggeringPermanentControllerId(combatDamageTargetControllers.get(damagedCreatureId));
                    trigger.setNonTargeting(true);
                    if (effect instanceof CombatExcessDamageAwareEffect) {
                        trigger.setEventValue(excessDamage);
                    }
                    gameData.stack.add(trigger);
                    gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
                }
            }
        }
    }

    private int combatExcessDamage(GameData gameData, CombatDamageState state, UUID damagedCreatureId,
                                   Permanent damagedCreatureSnapshot, int damage) {
        if (damage <= 0) {
            return 0;
        }
        int markedDamageBefore = state.markedDamageBeforeStep.getOrDefault(damagedCreatureId, 0);
        int lethalDamage = damagedCreatureSnapshot != null && damagedCreatureSnapshot.isDamagedByDeathtouch()
                ? 1
                : Math.max(0, state.lethalDamageThresholdBeforeStep.getOrDefault(damagedCreatureId,
                        damagedCreatureSnapshot == null
                                ? 0
                                : gameQueryService.getLethalDamageThreshold(gameData, damagedCreatureSnapshot))
                        - markedDamageBefore);
        return Math.max(0, damage - lethalDamage);
    }

    private void processCombatDamageToBlockingCreatureTriggers(GameData gameData, CombatDamageState state) {
        for (Permanent source : state.combatDamageDealtToCreatures.keySet()) {
            if (!state.combatDamageToBlockingCreatureSources.contains(source.getId())) {
                continue;
            }
            UUID controllerId = state.combatDamageDealerControllers.get(source);
            if (controllerId == null) {
                continue;
            }
            for (CardEffect effect : source.getCard().getEffects(
                    EffectSlot.ON_COMBAT_DAMAGE_TO_BLOCKING_CREATURE)) {
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, source.getCard(), controllerId,
                        source.getCard().getName() + "'s triggered ability", List.of(effect), null,
                        source.getId());
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);
                gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
            }
        }
    }

    /**
     * Fires Acidic Dagger's delayed "whenever that creature deals combat damage to a non-Wall
     * creature this turn, destroy that non-Wall creature" triggers. The registration is not drained:
     * it lasts the rest of the turn and fires once per damaged non-Wall creature in every combat
     * damage step, until turn cleanup clears it.
     */
    private void processDelayedDestroyCreatureDamagedTriggers(GameData gameData,
                                                              Map<Permanent, List<UUID>> combatDamageDealtToCreatures) {
        if (combatDamageDealtToCreatures.isEmpty()
                || !gameData.hasDelayedAction(DelayedDestroyCreatureDamagedByWatchedCreature.class)) {
            return;
        }

        for (DelayedDestroyCreatureDamagedByWatchedCreature delayed
                : gameData.getDelayedActions(DelayedDestroyCreatureDamagedByWatchedCreature.class)) {
            for (var entry : combatDamageDealtToCreatures.entrySet()) {
                if (!delayed.watchedPermanentId().equals(entry.getKey().getId())) continue;

                for (UUID damagedCreatureId : entry.getValue()) {
                    Permanent damaged = gameQueryService.findPermanentById(gameData, damagedCreatureId);
                    if (damaged == null || predicateEvaluationService.matchesPermanentPredicate(
                            gameData, damaged, new PermanentHasSubtypePredicate(CardSubtype.WALL))) {
                        continue;
                    }

                    StackEntry trigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            delayed.sourceCard(),
                            delayed.controllerId(),
                            delayed.sourceCard().getName() + "'s delayed trigger",
                            new ArrayList<>(List.of(new DestroyTargetPermanentEffect())),
                            damagedCreatureId,
                            (UUID) null);
                    trigger.setNonTargeting(true);
                    gameData.stack.add(trigger);
                    gameLogService.append(gameData, GameLog.abilityTriggers(delayed.sourceCard()));
                }
            }
        }
    }

    private void processDelayedPlaneswalkerCombatDamageTriggers(GameData gameData,
                                                                Map<Permanent, Integer> combatDamageDealt) {
        if (combatDamageDealt.isEmpty()
                || !gameData.hasDelayedAction(DelayedDestroyCreatureDealingCombatDamageToPlaneswalker.class)) {
            return;
        }

        for (DelayedDestroyCreatureDealingCombatDamageToPlaneswalker delayed
                : gameData.getDelayedActions(DelayedDestroyCreatureDealingCombatDamageToPlaneswalker.class)) {
            for (var entry : combatDamageDealt.entrySet()) {
                Permanent creature = entry.getKey();
                if (entry.getValue() <= 0
                        || !delayed.planeswalkerId().equals(creature.getAttackTarget())) {
                    continue;
                }

                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        delayed.sourceCard(),
                        delayed.controllerId(),
                        delayed.sourceCard().getName() + "'s delayed trigger",
                        List.of(new DestroyTargetPermanentEffect()),
                        creature.getId(),
                        (UUID) null
                );
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);
                gameLogService.append(gameData, GameLog.abilityTriggers(delayed.sourceCard()));
            }
        }
    }

    /**
     * Fires delayed triggers watching creatures for combat damage. {@code combatDamageDealt} already
     * sums each source's damage to every recipient, so a watched creature that damaged both a blocker
     * and a player triggers once. Player-only watches additionally use the player-specific damage map.
     * The trigger is controlled by the player who activated the ability, not by the creature's
     * controller, so "you draw a card" always draws for them.
     */
    private void processDelayedWatchedCreatureCombatDamageTriggers(GameData gameData,
                                                                   CombatDamageState state) {
        if (state.combatDamageDealt.isEmpty()
                || !gameData.hasDelayedAction(DelayedWatchedCreaturesCombatDamage.class)) {
            return;
        }

        for (DelayedWatchedCreaturesCombatDamage watch
                : gameData.getDelayedActions(DelayedWatchedCreaturesCombatDamage.class)) {
            for (var entry : state.combatDamageDealt.entrySet()) {
                if (entry.getValue() <= 0
                        || !watch.watchedPermanentIds().contains(entry.getKey().getId())
                        || (watch.combatDamageToPlayerOnly()
                        && state.combatDamageDealtToPlayer.getOrDefault(entry.getKey(), 0) <= 0)) {
                    continue;
                }

                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        watch.sourceCard(),
                        watch.controllerId(),
                        watch.sourceCard().getName() + "'s delayed trigger",
                        new ArrayList<>(watch.effects()),
                        (UUID) null,
                        entry.getKey().getId());
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);
                gameLogService.append(gameData, GameLog.abilityTriggers(watch.sourceCard()));
            }
        }
    }

    private void processDelayedNamedCreatureCombatDamageTriggers(GameData gameData,
                                                                  CombatDamageState state) {
        if (state.combatDamageDealtToPlayer.isEmpty()
                || !gameData.hasDelayedAction(DelayedNamedCreatureCombatDamage.class)) {
            return;
        }

        for (DelayedNamedCreatureCombatDamage watch
                : gameData.getDelayedActions(DelayedNamedCreatureCombatDamage.class)) {
            for (var entry : state.combatDamageDealtToPlayer.entrySet()) {
                Permanent creature = entry.getKey();
                if (entry.getValue() <= 0
                        || !watch.cardName().equals(gameQueryService.getEffectiveName(gameData, creature))) {
                    continue;
                }

                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        watch.sourceCard(),
                        watch.controllerId(),
                        watch.sourceCard().getName() + "'s delayed trigger",
                        new ArrayList<>(watch.effects()),
                        (UUID) null,
                        (UUID) null);
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);
                gameLogService.append(gameData, GameLog.abilityTriggers(watch.sourceCard()));
            }
        }
    }

    /**
     * Collects the self-scoped ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE triggers of each combat-damage
     * source. Runs before state-based actions so a source that dies to the same damage still triggers.
     */
    private void processSelfDealtDamageToCreatureReflectionTriggers(GameData gameData, CombatDamageState state,
                                                                     Map<UUID, Permanent> damagedCreatureSnapshots) {
        forEachCombatDamageToCreaturePair(gameData, state, damagedCreatureSnapshots,
                (source, sourceControllerId, damagedCreatureControllerId, damagedCreatureId,
                 damagedCreatureSnapshot, damage) ->
                        triggerCollectionService.checkSelfDealtDamageToCreatureTriggers(
                                gameData, source, sourceControllerId, damagedCreatureControllerId,
                                damagedCreatureId, damagedCreatureSnapshot, damage, true));
    }

    /**
     * Collects the ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE triggers that other permanents watch
     * (e.g. Greatbow Doyen, Ohabi Caleria) for each source/target combat-damage pair. Runs before
     * state-based actions so a watcher that dies to the same damage still triggers.
     */
    private void processWatchersDealtDamageToCreatureReflectionTriggers(GameData gameData, CombatDamageState state,
                                                                         Map<UUID, Permanent> damagedCreatureSnapshots) {
        forEachCombatDamageToCreaturePair(gameData, state, damagedCreatureSnapshots,
                (source, sourceControllerId, damagedCreatureControllerId, damagedCreatureId,
                 damagedCreatureSnapshot, damage) ->
                        triggerCollectionService.checkWatchersDealtDamageToCreatureTriggers(
                                gameData, source, sourceControllerId, damagedCreatureControllerId,
                                damagedCreatureId, damagedCreatureSnapshot, damage, true));
    }

    /**
     * Fires the creature-damaged-by-source triggers that depend on the damaged creature's snapshot
     * for each source/target combat-damage pair.
     */
    private void processAllyDealtDamageToCreatureReflectionTriggers(GameData gameData, CombatDamageState state,
                                                                     Map<UUID, Permanent> damagedCreatureSnapshots) {
        forEachCombatDamageToCreaturePair(gameData, state, damagedCreatureSnapshots,
                (source, sourceControllerId, damagedCreatureControllerId, damagedCreatureId,
                 damagedCreatureSnapshot, damage) -> {
                    if (damagedCreatureSnapshot != null) {
                        triggerCollectionService.checkCreatureDamageToYouOrYourPermanentTriggers(
                                gameData, damagedCreatureControllerId, damagedCreatureSnapshot, source, damage);
                    }
                });
    }

    private void forEachCombatDamageToCreaturePair(GameData gameData, CombatDamageState state,
                                                    Map<UUID, Permanent> damagedCreatureSnapshots,
                                                    CombatDamagePairAction action) {
        for (var entry : state.combatDamageAmountsToCreatures.entrySet()) {
            Permanent source = entry.getKey();
            UUID sourceControllerId = state.combatDamageDealerControllers.get(source);
            if (sourceControllerId == null) continue;
            for (var amountEntry : entry.getValue().entrySet()) {
                UUID damagedCreatureControllerId = state.combatDamageTargetControllers.get(amountEntry.getKey());
                Permanent damagedCreatureSnapshot = gameQueryService.findPermanentById(gameData, amountEntry.getKey());
                if (damagedCreatureSnapshot == null) {
                    damagedCreatureSnapshot = damagedCreatureSnapshots.get(amountEntry.getKey());
                }
                action.accept(source, sourceControllerId, damagedCreatureControllerId, amountEntry.getKey(),
                        damagedCreatureSnapshot, amountEntry.getValue());
            }
        }
    }

    @FunctionalInterface
    private interface CombatDamagePairAction {
        void accept(Permanent source, UUID sourceControllerId, UUID damagedCreatureControllerId,
                    UUID damagedCreatureId, Permanent damagedCreatureSnapshot, int damage);
    }

    private void checkGraveyardAllyCreatureCombatDamageTriggers(GameData gameData, Permanent creature,
                                                                 UUID attackerId, EffectSlot slot,
                                                                 Set<UUID> firedBatchedAllyTriggerSources) {
        List<Card> graveyard = gameData.playerGraveyards.get(attackerId);
        if (graveyard == null) return;
        for (Card card : new ArrayList<>(graveyard)) {
            for (CardEffect effect : gameQueryService.getEffectiveGraveyardEffects(gameData, card, slot)) {
                if (!(effect instanceof AllyCombatDamageTriggerEffect trigger)) {
                    continue;
                }
                if (trigger.dealerPredicate() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(gameData, creature,
                        trigger.dealerPredicate())) {
                    continue;
                }
                if (trigger.oncePerDamageStep() && !firedBatchedAllyTriggerSources.add(card.getId())) {
                    continue;
                }
                StackEntry stackEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        attackerId,
                        card.getName() + "'s graveyard trigger",
                        List.of(trigger.effect()));
                stackEntry.setTriggeringCardId(card.getId());
                stackEntry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
                stackEntry.setNonTargeting(true);
                gameData.stack.add(stackEntry);
                gameLogService.append(gameData, GameLog.cardThen(card,
                        "'s graveyard trigger goes on the stack."));
            }
        }
    }

    private void processAllyDealtDamageToPlaneswalkerTriggers(GameData gameData, CombatDamageState state) {
        Set<UUID> firedBatchedAllyTriggerSources = new HashSet<>();
        for (var entry : state.combatDamageAmountsToPlaneswalkers.entrySet()) {
            Permanent source = entry.getKey();
            UUID sourceControllerId = state.combatDamageDealerControllers.get(source);
            if (sourceControllerId == null) continue;
            for (var amountEntry : entry.getValue().entrySet()) {
                if (amountEntry.getValue() > 0) {
                    checkGraveyardAllyCreatureCombatDamageTriggers(gameData, source, sourceControllerId,
                            EffectSlot.GRAVEYARD_ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER,
                            firedBatchedAllyTriggerSources);
                }
                triggerCollectionService.checkAllyDealtDamageToPlaneswalkerTriggers(
                        gameData, source, sourceControllerId, amountEntry.getKey(), amountEntry.getValue(), true,
                        state.allyCreatureDealsDamageToPlaneswalkerTriggers);
            }
        }
    }

    /**
     * Captures the damaged creatures before state-based actions remove lethal casualties.
     */
    private Map<UUID, Permanent> snapshotCombatDamagedCreatures(GameData gameData, CombatDamageState state) {
        Map<UUID, Permanent> damagedCreatures = new LinkedHashMap<>();
        for (var entry : state.combatDamageAmountsToCreatures.entrySet()) {
            for (var amountEntry : entry.getValue().entrySet()) {
                if (amountEntry.getValue() <= 0) continue;
                Permanent damaged = gameQueryService.findPermanentById(gameData, amountEntry.getKey());
                if (damaged != null) {
                    damagedCreatures.putIfAbsent(damaged.getId(), damaged);
                }
            }
        }
        return damagedCreatures;
    }

    /**
     * Fires ON_ANY_CREATURE_DEALT_DAMAGE triggers for each creature that took combat damage this
     * step, using the captured creature and controller when lethal damage removed it.
     */
    private void processAnyCreatureDealtDamageTriggers(GameData gameData, CombatDamageState state,
                                                       Map<UUID, Permanent> damagedCreatureSnapshots) {
        Map<UUID, Integer> damageByCreature = new LinkedHashMap<>();
        for (var entry : state.combatDamageDealtToCreatures.entrySet()) {
            Map<UUID, Integer> amounts = state.combatDamageAmountsToCreatures.getOrDefault(entry.getKey(), Map.of());
            for (UUID targetId : entry.getValue()) {
                int damage = amounts.getOrDefault(targetId, 0);
                if (damage > 0) {
                    damageByCreature.merge(targetId, damage, Integer::sum);
                }
            }
        }
        for (var entry : damageByCreature.entrySet()) {
            UUID damagedCreatureId = entry.getKey();
            Permanent damaged = gameQueryService.findPermanentById(gameData, damagedCreatureId);
            if (damaged == null) {
                damaged = damagedCreatureSnapshots.get(damagedCreatureId);
            }
            if (damaged == null) continue;

            UUID controllerId = state.combatDamageTargetControllers.get(damagedCreatureId);
            triggerCollectionService.checkAnyCreatureDealtDamageTriggers(
                    gameData, damaged, controllerId, entry.getValue());
            triggerCollectionService.checkTemporaryGlobalOpponentCreatureDealtDamageTriggers(
                    gameData, damaged, controllerId, entry.getValue());
        }
    }

    private record DealtDamageTriggerData(Card card, UUID permanentId, UUID controllerId, int damageDealt,
                                          Permanent sourceSnapshot,
                                          Card sourceCard, UUID sourcePermanentId, UUID sourceControllerId,
                                          List<CardEffect> dealtDamageEffects,
                                          List<CardEffect> combatDamageReceivedEffects) {}

    private List<DealtDamageTriggerData> collectDealtDamageTriggerData(GameData gameData, CombatDamageState state) {
        Map<UUID, DealtDamageTriggerData> triggersByDamagedPermanent = new LinkedHashMap<>();
        List<DealtDamageTriggerData> sourceSpecificTriggers = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (var entry : state.combatDamageDealtToCreatures.entrySet()) {
            Permanent source = entry.getKey();
            UUID sourceControllerId = state.combatDamageDealerControllers.get(source);
            Map<UUID, Integer> damageAmounts = state.combatDamageAmountsToCreatures.getOrDefault(source, Map.of());
            for (UUID targetId : entry.getValue()) {
                // Deduplicate: same source + target pair may appear twice (double strike phases)
                String key = source.getId() + ":" + targetId;
                if (!seen.add(key)) continue;

                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target == null) continue;
                boolean hasPrintedAbilities = !target.isFaceDown()
                        && !gameQueryService.hasLostPrintedAbilities(gameData, target);
                List<CardEffect> effects = new ArrayList<>();
                if (hasPrintedAbilities) {
                    effects.addAll(target.getCard().getEffects(EffectSlot.ON_DEALT_DAMAGE));
                }
                effects.addAll(target.getTemporaryTriggeredEffects(EffectSlot.ON_DEALT_DAMAGE));
                effects.addAll(target.getPersistentTriggeredEffects(EffectSlot.ON_DEALT_DAMAGE));
                effects.addAll(grantedTriggeredAbilitySupport.grantedTriggeredEffects(
                        gameData, target, EffectSlot.ON_DEALT_DAMAGE));
                List<CardEffect> combatDamageReceivedEffects = new ArrayList<>();
                if (hasPrintedAbilities) {
                    combatDamageReceivedEffects.addAll(target.getCard().getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_SELF));
                }
                combatDamageReceivedEffects.addAll(target.getTemporaryTriggeredEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_SELF));
                combatDamageReceivedEffects.addAll(target.getPersistentTriggeredEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_SELF));
                UUID controllerId = gameData.findControllerOf(target);
                if (controllerId == null) continue;
                int damageAmount = damageAmounts.getOrDefault(targetId, 0);
                List<CardEffect> sourceSpecificEffects = effects.stream()
                        .filter(effect -> requiresDistinctDamageSource(effect))
                        .toList();
                effects.removeAll(sourceSpecificEffects);
                if (!sourceSpecificEffects.isEmpty()) {
                    sourceSpecificTriggers.add(new DealtDamageTriggerData(
                            target.getCard(), target.getId(), controllerId, damageAmount, new Permanent(target),
                            source.getCard(), source.getId(), sourceControllerId,
                            sourceSpecificEffects, List.of()));
                }
                if (effects.isEmpty() && combatDamageReceivedEffects.isEmpty()) continue;
                DealtDamageTriggerData previous = triggersByDamagedPermanent.get(targetId);
                if (previous == null) {
                    triggersByDamagedPermanent.put(targetId, new DealtDamageTriggerData(
                            target.getCard(), target.getId(), controllerId, damageAmount, new Permanent(target),
                            source.getCard(), source.getId(), sourceControllerId,
                            effects, combatDamageReceivedEffects));
                } else {
                    triggersByDamagedPermanent.put(targetId, new DealtDamageTriggerData(
                            previous.card(), previous.permanentId(), previous.controllerId(),
                            previous.damageDealt() + damageAmount, previous.sourceSnapshot(),
                            previous.sourceCard(), previous.sourcePermanentId(), previous.sourceControllerId(),
                            previous.dealtDamageEffects(), previous.combatDamageReceivedEffects()));
                }
            }
        }
        sourceSpecificTriggers.addAll(triggersByDamagedPermanent.values());
        return sourceSpecificTriggers;
    }

    private boolean requiresDistinctDamageSource(CardEffect effect) {
        if (effect instanceof com.github.laxika.magicalvibes.model.effect.SequenceEffect sequence) {
            return sequence.steps().stream().anyMatch(this::requiresDistinctDamageSource);
        }
        if (effect instanceof ConditionalEffect conditional) {
            return requiresDistinctDamageSource(conditional.wrapped());
        }
        if (effect instanceof MayEffect may) {
            return requiresDistinctDamageSource(may.wrapped());
        }
        return effect instanceof DamageSourceAwareEffect
                || effect instanceof DamageSourceControllerAwareEffect
                || effect instanceof PerDamageSourceTriggerEffect;
    }

    private void processDealtDamageTriggers(GameData gameData, List<DealtDamageTriggerData> triggerData) {
        for (DealtDamageTriggerData data : triggerData) {
            for (CardEffect authoredEffect : data.dealtDamageEffects()) {
                CardEffect effect = OncePerTurnTriggerSupport.unwrapIfAvailable(
                        gameData, data.sourceSnapshot(), authoredEffect);
                if (effect == null) continue;
                OncePerTurnTriggerSupport.markIfNeeded(gameData, data.sourceSnapshot(), authoredEffect);
                CardEffect effectToAdd = effect;
                if (effect instanceof PerDamageSourceTriggerEffect perSource) {
                    effectToAdd = perSource.effect();
                } else if (effect instanceof DamageSourceAwareEffect aware) {
                    effectToAdd = aware.bindDamageSource(
                            data.sourceCard(), data.sourcePermanentId(), data.sourceControllerId(), data.damageDealt());
                } else if (effect instanceof DamageSourceControllerAwareEffect aware) {
                    effectToAdd = aware.bindDamageSourceController(data.sourceControllerId(), data.damageDealt(), data.sourcePermanentId());
                } else if (effect instanceof DealDamageToTargetPlayerOrPlaneswalkerEffect burn
                        && burn.playerRelation() == PlayerRelation.OPPONENT) {
                    // Targeting effect — auto-target opponent when no planeswalkers, otherwise queue for choice.
                    // Only the opponent-only wording can be auto-targeted; the plain "target player"
                    // form has no single implied player and falls through to the generic tail.
                    boolean hasPlaneswalkers = false;
                    for (UUID pid : gameData.orderedPlayerIds) {
                        List<Permanent> bf = gameData.playerBattlefields.get(pid);
                        if (bf != null) {
                            for (Permanent p : bf) {
                                if (p.getCard().hasType(CardType.PLANESWALKER)) {
                                    hasPlaneswalkers = true;
                                    break;
                                }
                            }
                        }
                        if (hasPlaneswalkers) break;
                    }
                    if (!hasPlaneswalkers) {
                        UUID opponentId = gameQueryService.getOpponentId(gameData, data.controllerId());
                        StackEntry entry = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                data.card(),
                                data.controllerId(),
                                data.card().getName() + "'s ability",
                                new ArrayList<>(List.of(effectToAdd)),
                                data.damageDealt(),
                                data.permanentId()
                        );
                        entry.setTargetId(opponentId);
                        gameData.stack.add(entry);
                    } else {
                        TargetFilter targetFilter = data.card().getEffectTargetIndex(effectToAdd) >= 0
                                ? data.card().getTargetFilter() : null;
                        gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                                data.card(), data.controllerId(), new ArrayList<>(List.of(effectToAdd)),
                                false, targetFilter, data.damageDealt()
                        ));
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(data.card()));
                    log.info("Game {} - {} ON_DEALT_DAMAGE combat trigger fires", gameData.id, data.card().getName());
                    continue;
                } else if (effect instanceof DealDamageToAnyTargetEffect damageEffect) {
                    // "It deals that much damage to any target" (Spitemare): the damage amount
                    // snapshots into the trigger event value, and the controller chooses any target
                    // when serviced.
                    if (data.damageDealt() > 0) {
                        Permanent source = gameQueryService.findPermanentById(gameData, data.permanentId());
                        gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                                data.card(), data.controllerId(), new ArrayList<>(List.of(effect)),
                                false, damageEffect.triggeredTargetFilter(), data.damageDealt(), data.permanentId(),
                                source == null ? null : new Permanent(source)));
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(data.card()));
                    log.info("Game {} - {} ON_DEALT_DAMAGE combat trigger fires", gameData.id, data.card().getName());
                    continue;
                }

                if (effectToAdd.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        || effectToAdd.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
                    TargetFilter targetFilter = data.card().getEffectTargetIndex(effect) >= 0
                            ? data.card().getTargetFilter() : null;
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            data.card(), data.controllerId(), new ArrayList<>(List.of(effectToAdd)),
                            !effectToAdd.targetSpec().admits(TargetPredicate.Kind.PERMANENT),
                            targetFilter, data.damageDealt(), data.permanentId()));
                    gameLogService.append(gameData, GameLog.abilityTriggers(data.card()));
                    log.info("Game {} - {} ON_DEALT_DAMAGE combat trigger awaits target",
                            gameData.id, data.card().getName());
                    continue;
                }

                // The combat damage dealt snapshots onto eventValue so "it deals that much damage"
                // effects can read it with an EventValue amount (Stuffy Doll).
                StackEntry triggerEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        data.card(),
                        data.controllerId(),
                        data.card().getName() + "'s ability",
                        new ArrayList<>(List.of(effectToAdd)),
                        null,
                        data.permanentId()
                );
                triggerEntry.setEventValue(data.damageDealt());
                triggerEntry.setSourcePermanentSnapshot(data.sourceSnapshot());
                gameData.stack.add(triggerEntry);
                gameLogService.append(gameData, GameLog.abilityTriggers(data.card()));
                log.info("Game {} - {} ON_DEALT_DAMAGE combat trigger fires", gameData.id, data.card().getName());
            }
        }
    }

    private void processCombatDamageReceivedTriggers(GameData gameData,
                                                      List<DealtDamageTriggerData> triggerData) {
        for (DealtDamageTriggerData data : triggerData) {
            if (data.damageDealt() <= 0) continue;

            for (CardEffect effect : data.combatDamageReceivedEffects()) {
                CardEffect effectToAdd = effect instanceof DamageSourceControllerAwareEffect aware
                        ? aware.bindDamageSourceController(data.sourceControllerId(), data.damageDealt(), data.sourcePermanentId())
                        : effect;
                if (effectToAdd.targetSpec().declaredTarget() != null) {
                    TargetFilter targetFilter = data.card().getEffectTargetIndex(effect) >= 0
                            ? data.card().getTargetFilter() : null;
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            data.card(), data.controllerId(), new ArrayList<>(List.of(effectToAdd)), false,
                            targetFilter, data.damageDealt(), data.permanentId()));
                    gameLogService.append(gameData,
                            GameLog.cardThen(data.card(), "'s combat-damage trigger fires — choose a target."));
                    continue;
                }

                StackEntry triggerEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        data.card(),
                        data.controllerId(),
                        data.card().getName() + "'s ability",
                        new ArrayList<>(List.of(effectToAdd)),
                        null,
                        data.permanentId()
                );
                triggerEntry.setEventValue(data.damageDealt());
                gameData.stack.add(triggerEntry);
                gameLogService.append(gameData, GameLog.abilityTriggers(data.card()));
                log.info("Game {} - {} combat-damage-received trigger fires", gameData.id,
                        data.card().getName());
            }
        }
    }

    private void captureCombatDamageBaselines(GameData gameData, CombatDamageState state,
                                              List<Permanent> atkBf, List<Permanent> defBf) {
        for (Permanent permanent : atkBf) {
            captureCombatDamageBaseline(gameData, state, permanent);
        }
        for (Permanent permanent : defBf) {
            captureCombatDamageBaseline(gameData, state, permanent);
        }
        for (Permanent attacker : atkBf) {
            UUID attackTarget = attacker.getAttackTarget();
            if (attackTarget != null && !gameData.playerIds.contains(attackTarget)) {
                captureCombatDamageBaseline(gameData, state,
                        gameQueryService.findPermanentById(gameData, attackTarget));
            }
        }
    }

    private void captureCombatDamageBaseline(GameData gameData, CombatDamageState state,
                                             Permanent permanent) {
        if (permanent == null || state.damageDealtToPermanentsBeforeStep.containsKey(permanent.getId())) {
            return;
        }
        UUID permanentId = permanent.getId();
        state.damageDealtToPermanentsBeforeStep.put(permanentId,
                gameData.damageDealtToPermanentsThisTurn.getOrDefault(permanentId, 0));
        state.markedDamageBeforeStep.put(permanentId, permanent.getMarkedDamage());
        state.toughnessBeforeStep.put(permanentId, gameQueryService.getEffectiveToughness(gameData, permanent));
        state.lethalDamageThresholdBeforeStep.put(permanentId,
                gameQueryService.getLethalDamageThreshold(gameData, permanent));
        state.loyaltyBeforeStep.put(permanentId, permanent.getCounterCount(CounterType.LOYALTY));
    }

    private void checkCombatExcessDamageTriggers(GameData gameData, CombatDamageState state,
                                                 List<Permanent> atkBf, List<Permanent> defBf) {
        Set<UUID> processed = new HashSet<>();
        for (UUID permanentId : state.damageToPlaneswalkers.keySet()) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent == null || !gameQueryService.isPlaneswalker(gameData, permanent)) continue;
            int damage = gameData.damageDealtToPermanentsThisTurn.getOrDefault(permanentId, 0)
                    - state.damageDealtToPermanentsBeforeStep.getOrDefault(permanentId, 0);
            if (damage <= 0) continue;
            int lethalDamage = gameQueryService.isToughnessAsLoyaltyPermanent(gameData, permanent)
                    ? state.toughnessBeforeStep.getOrDefault(permanentId, 0)
                    : state.loyaltyBeforeStep.getOrDefault(permanentId, 0);
            triggerCollectionService.checkOpponentPermanentDealtExcessDamageTriggers(
                    gameData, permanent, gameQueryService.findPermanentController(gameData, permanentId),
                    Math.max(0, damage - lethalDamage));
            processed.add(permanentId);
        }

        for (Permanent permanent : concatPermanents(atkBf, defBf)) {
            if (processed.contains(permanent.getId()) || !gameQueryService.isCreature(gameData, permanent)) continue;
            UUID permanentId = permanent.getId();
            int damage = gameData.damageDealtToPermanentsThisTurn.getOrDefault(permanentId, 0)
                    - state.damageDealtToPermanentsBeforeStep.getOrDefault(permanentId, 0);
            if (damage <= 0) continue;
            int markedDamageBefore = state.markedDamageBeforeStep.getOrDefault(permanentId, 0);
            int lethalDamage = permanent.isDamagedByDeathtouch()
                    ? 1
                    : Math.max(0, state.lethalDamageThresholdBeforeStep.getOrDefault(permanentId,
                            gameQueryService.getLethalDamageThreshold(gameData, permanent)) - markedDamageBefore);
            int excessDamage = Math.max(0, damage - lethalDamage);
            if (excessDamage > 0) {
                gameData.recordExcessDamageToPermanent(permanentId);
            }
            triggerCollectionService.checkOpponentPermanentDealtExcessDamageTriggers(
                    gameData, permanent, gameQueryService.findPermanentController(gameData, permanentId),
                    excessDamage);
        }
    }

    private static List<Permanent> concatPermanents(List<Permanent> first, List<Permanent> second) {
        List<Permanent> result = new ArrayList<>(first.size() + second.size());
        result.addAll(first);
        result.addAll(second);
        return result;
    }

    /** Updates marked damage on creatures from combat damage maps. */
    private void updateMarkedDamageFromCombat(GameData gameData, List<Permanent> atkBf, List<Permanent> defBf,
                                               CombatDamageState state) {
        applyStepDamageToPermanents(gameData, atkBf, state.atkDamageTaken,
                state.unpreventableAtkDamageTaken, state.deathtouchDamagedAttackerIndices,
                state.atkDamageTakenBySource, state);
        applyStepDamageToPermanents(gameData, defBf, state.defDamageTaken,
                state.unpreventableDefDamageTaken, state.deathtouchDamagedDefenderIndices,
                state.defDamageTakenBySource, state);
        for (Permanent permanent : state.permanentsToShuffleAfterDamage.values()) {
            reconcileCombatDamageToCreature(state, permanent.getId(), Map.of());
            permanentRemovalService.removePermanentToLibraryShuffled(gameData, permanent);
        }
        state.permanentsToShuffleAfterDamage.clear();
    }

    /**
     * Applies one damage step's accumulated combat damage to the permanents as marked damage
     * (CR 704.5g) plus the CR 704.5h deathtouch memory. Replacement and prevention effects have
     * already modified each creature's damage as one event.
     */
    private void applyStepDamageToPermanents(GameData gameData, List<Permanent> battlefield,
                                              Map<Integer, Integer> damageTaken,
                                              Map<Integer, Integer> unpreventableDamageTaken,
                                              Set<Integer> deathtouchIndices,
                                              Map<Integer, Map<UUID, Integer>> damageTakenBySource,
                                              CombatDamageState state) {
        for (var entry : damageTaken.entrySet()) {
            int idx = entry.getKey();
            if (idx >= battlefield.size()) continue;
            Permanent perm = battlefield.get(idx);
            int dmg = entry.getValue();
            Map<UUID, Integer> bySource = new LinkedHashMap<>(
                    damageTakenBySource.getOrDefault(idx, Map.of()));
            if (dmg > 0) {
                damagePreventionService.applyDamageHealingReplacement(gameData, perm, dmg);
                boolean toughnessAsLoyalty = gameQueryService.isToughnessAsLoyaltyPermanent(gameData, perm);
                if (toughnessAsLoyalty) {
                    perm.setToughnessAsLoyalty(
                            gameQueryService.getToughnessAsLoyalty(perm) - dmg);
                }
                Map<UUID, Integer> attributedDamage = recordCombatMarkedDamage(
                        perm, dmg, bySource, !toughnessAsLoyalty);
                reconcileCombatDamageToCreature(state, perm.getId(), attributedDamage);
                gameData.recordDamageToPermanent(perm.getId(), dmg);
                triggerCollectionService.checkAnyPermanentDealtDamageTriggers(gameData, perm, dmg);
                attributedDamage.forEach((sourceId, amount) -> {
                    Permanent damageSource = gameQueryService.findPermanentById(gameData, sourceId);
                    UUID sourceControllerId = state.combatDamageDealerControllers.get(damageSource);
                    if (sourceControllerId == null && damageSource != null) {
                        sourceControllerId = gameQueryService.findPermanentController(gameData, sourceId);
                    }
                    gameData.recordDamageToPermanentFromSource(perm.getId(), amount, sourceId,
                            damageSource == null ? null : gameQueryService.getEffectiveName(gameData, damageSource),
                            sourceControllerId);
                    if (gameQueryService.isCreature(gameData, perm)) {
                        gameData.recordDamageDealtToCreatureBySource(sourceId, perm.getId());
                        graveyardService.recordCreatureDamagedByPermanent(gameData, sourceId, perm, amount);
                    }
                });
                damageTakenBySource.getOrDefault(idx, Map.of()).keySet()
                        .forEach(sourceId -> recordQualifyingCombatDamageBySourceId(gameData, sourceId, perm));
                // CR 702.2b — the deathtouch memory only sticks when damage was actually dealt,
                // not when a prevention shield consumed the whole step's damage.
                if (!toughnessAsLoyalty && deathtouchIndices.contains(idx)) {
                    perm.setDamagedByDeathtouch(true);
                }
            }
            if (dmg == 0) {
                reconcileCombatDamageToCreature(state, perm.getId(), Map.of());
            }
        }
    }

    /** Updates damage-trigger and lifelink totals after creature damage prevention. */
    private static void reconcileCombatDamageToCreature(CombatDamageState state, UUID targetId,
                                                        Map<UUID, Integer> actualBySource) {
        for (var sourceEntry : state.combatDamageAmountsToCreatures.entrySet()) {
            Permanent source = sourceEntry.getKey();
            Map<UUID, Integer> amounts = sourceEntry.getValue();
            Integer assigned = amounts.get(targetId);
            if (assigned == null) continue;
            int dealt = actualBySource.getOrDefault(source.getId(), 0);
            state.combatDamageDealt.computeIfPresent(source,
                    (ignored, total) -> Math.max(0, total - assigned + dealt));
            if (dealt == 0) {
                amounts.remove(targetId);
                List<UUID> recipients = state.combatDamageDealtToCreatures.get(source);
                if (recipients != null) recipients.removeIf(targetId::equals);
            } else {
                amounts.put(targetId, dealt);
            }
        }
    }

    private void applyPendingDralnuReplacements(GameData gameData, CombatDamageState state) {
        for (var entry : state.pendingDralnuReplacementTargets.entrySet()) {
            damageSupport.applyDralnuReplacement(gameData, entry.getValue(),
                    state.pendingDralnuReplacementDamage.getOrDefault(entry.getKey(), 0));
        }
        state.pendingDralnuReplacementTargets.clear();
        state.pendingDralnuReplacementDamage.clear();
    }

    private static void queueDralnuReplacement(CombatDamageState state, Permanent target, int damage) {
        state.pendingDralnuReplacementTargets.putIfAbsent(target.getId(), target);
        state.pendingDralnuReplacementDamage.merge(target.getId(), damage, Integer::sum);
    }

    /**
     * Marks {@code dmg} on {@code perm}, attributing it across {@code bySource} (scaled when a
     * prevention shield reduced the step total below the sum of per-source contributions).
     */
    private static Map<UUID, Integer> recordCombatMarkedDamage(Permanent perm, int dmg,
                                                                Map<UUID, Integer> bySource,
                                                                boolean markDamage) {
        if (bySource.isEmpty()) {
            if (markDamage) perm.addMarkedDamage(null, dmg);
            return Map.of();
        }
        int sourceSum = bySource.values().stream().mapToInt(Integer::intValue).sum();
        if (sourceSum <= 0) {
            if (markDamage) perm.addMarkedDamage(null, dmg);
            return Map.of();
        }
        if (sourceSum == dmg) {
            if (markDamage) bySource.forEach(perm::addMarkedDamage);
            return Map.copyOf(bySource);
        }
        int remaining = dmg;
        Map<UUID, Integer> attributedDamage = new LinkedHashMap<>();
        List<Map.Entry<UUID, Integer>> entries = new ArrayList<>(bySource.entrySet());
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<UUID, Integer> e = entries.get(i);
            int portion = (i == entries.size() - 1)
                    ? remaining
                    : (int) Math.round((double) e.getValue() * dmg / sourceSum);
            portion = Math.max(0, Math.min(portion, remaining));
            if (portion > 0) {
                if (markDamage) perm.addMarkedDamage(e.getKey(), portion);
                attributedDamage.put(e.getKey(), portion);
                remaining -= portion;
            }
        }
        if (remaining > 0 && markDamage) {
            perm.addMarkedDamage(null, remaining);
        }
        return Map.copyOf(attributedDamage);
    }

    /**
     * Removes dead permanent IDs from surviving blockers' blockingTargetIds.
     * If a blocker has no remaining blocking targets, clears its blocking state entirely.
     */
    private void clearOrphanedBlockingState(List<Permanent> battlefield, Set<UUID> deadIds) {
        if (deadIds.isEmpty()) return;
        for (Permanent p : battlefield) {
            if (!p.isBlocking()) continue;
            p.getBlockingTargetIds().removeAll(deadIds);
            if (p.getBlockingTargetIds().isEmpty()) {
                p.setBlocking(false);
                p.getBlockingTargets().clear();
            }
        }
    }

    /** Applies aggregate prevention independently to each player before lifelink and state-based actions. */
    private void applyDamageToEachPlayer(GameData gameData, CombatDamageState state, UUID defaultDefenderId) {
        Set<UUID> recipientIds = new LinkedHashSet<>(state.normalDamageToPlayers.keySet());
        recipientIds.addAll(state.infectDamageToPlayers.keySet());
        if (recipientIds.isEmpty()) {
            applyPlayerDamage(gameData, state, defaultDefenderId);
            state.infectDamageToPlayers.put(defaultDefenderId, state.poisonDamageToDefendingPlayer);
            return;
        }
        Map<UUID, Boolean> damageAsInfect = new LinkedHashMap<>();
        recipientIds.forEach(id -> damageAsInfect.put(id,
                gameQueryService.shouldDamageBeDealtAsInfect(gameData, id)));
        int totalNormal = 0;
        int totalInfect = 0;
        for (UUID recipientId : recipientIds) {
            CombatDamageState recipientState = new CombatDamageState();
            recipientState.damageToDefendingPlayer = state.normalDamageToPlayers.getOrDefault(recipientId, 0);
            recipientState.poisonDamageToDefendingPlayer = state.infectDamageToPlayers.getOrDefault(recipientId, 0);
            recipientState.unpreventableDamageToDefendingPlayer =
                    state.unpreventableDamageToPlayers.getOrDefault(recipientId, 0);
            recipientState.defenderDamageAsInfect = damageAsInfect.get(recipientId);
            state.combatDamageDealtToPlayer.forEach((source, amount) -> {
                if (recipientId.equals(state.combatDamagePlayerRecipients.getOrDefault(source, defaultDefenderId))) {
                    recipientState.combatDamageDealtToPlayer.put(source, amount);
                    recipientState.combatDamageDealt.put(source, state.combatDamageDealt.getOrDefault(source, amount));
                }
            });
            applyPlayerDamage(gameData, recipientState, recipientId);
            state.combatDamageDealtToPlayer.putAll(recipientState.combatDamageDealtToPlayer);
            state.combatDamageDealt.putAll(recipientState.combatDamageDealt);
            recipientState.combatDamageRecipientControllers.forEach((source, controllers) ->
                    state.combatDamageRecipientControllers.computeIfAbsent(source, ignored -> new HashSet<>())
                            .addAll(controllers));
            state.normalDamageToPlayers.put(recipientId, recipientState.damageToDefendingPlayer);
            state.infectDamageToPlayers.put(recipientId, recipientState.poisonDamageToDefendingPlayer);
            totalNormal += recipientState.damageToDefendingPlayer;
            totalInfect += recipientState.poisonDamageToDefendingPlayer;
        }
        state.damageToDefendingPlayer = totalNormal;
        state.poisonDamageToDefendingPlayer = totalInfect;
    }

    /**
     * Deals the step's combat damage to {@code defenderId}: replacement and prevention effects have
     * already modified it as one event per recipient (see {@link #processCollectedCombatDamage}).
     */
    private void applyPlayerDamage(GameData gameData, CombatDamageState state, UUID defenderId) {
        int artifactDamage = state.combatDamageDealtToPlayer.entrySet().stream()
                .filter(entry -> gameQueryService.isArtifact(gameData, entry.getKey()))
                .mapToInt(Map.Entry::getValue)
                .sum();
        // Phyrexian Unlife: convert normal combat damage to poison when at 0 or less life.
        // Uses pre-lifelink snapshot (CR 510.1: all combat damage is simultaneous).
        if (state.damageToDefendingPlayer > 0 && state.defenderDamageAsInfect) {
            state.poisonDamageToDefendingPlayer += state.damageToDefendingPlayer;
            state.damageToDefendingPlayer = 0;
        }
        if (state.damageToDefendingPlayer > 0) {
            if (gameQueryService.canPlayerLifeChange(gameData, defenderId)
                    && !gameQueryService.damageDoesNotCauseLifeLoss(gameData, defenderId)
                    && lifeSupport.replaceLifeLossWithShield(gameData, defenderId)) {
                gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(defenderId)
                        + " takes " + state.damageToDefendingPlayer + " combat damage."));
            } else if (gameQueryService.canPlayerLifeChange(gameData, defenderId)
                    && !gameQueryService.damageDoesNotCauseLifeLoss(gameData, defenderId)) {
                int currentLife = gameData.getLife(defenderId);
                int lifeAfterDamage = currentLife - state.damageToDefendingPlayer;
                // Worship / Elderscale Wurm: combat damage can't reduce the player's life total past an
                // active floor. The full damage is still dealt; only the life reduction is capped.
                // 0 means no active floor — do not clamp (life may go negative).
                int lifeFloor = gameQueryService.damageLifeFloor(gameData, defenderId, currentLife);
                if (lifeFloor > 0 && lifeAfterDamage < lifeFloor) {
                    lifeAfterDamage = lifeFloor;
                }
                int lifeLoss = (currentLife - lifeAfterDamage)
                        * gameQueryService.opponentLifeLossMultiplier(gameData, defenderId);
                int newLife = currentLife - lifeLoss;
                gameData.playerLifeTotals.put(defenderId, newLife);
                int lifeLost = currentLife - newLife;
                String logEntry = gameData.playerIdToName.get(defenderId) + " takes " + state.damageToDefendingPlayer + " combat damage.";
                gameLogService.append(gameData, GameLog.text(logEntry));
                if (lifeLost > 0) {
                    triggerCollectionService.checkLifeLossTriggers(gameData, defenderId, lifeLost);
                }
            } else {
                String playerName = gameData.playerIdToName.get(defenderId);
                String message = gameQueryService.damageDoesNotCauseLifeLoss(gameData, defenderId)
                        ? playerName + " takes " + state.damageToDefendingPlayer + " combat damage."
                        : playerName + "'s life total can't change.";
                gameLogService.append(gameData, GameLog.text(message));
            }
        }

        // Track that the defending player was dealt damage this turn (for Bloodcrazed Goblin etc.)
        if (state.damageToDefendingPlayer > 0 || state.poisonDamageToDefendingPlayer > 0) {
            int damageDealt = state.damageToDefendingPlayer + state.poisonDamageToDefendingPlayer;
            for (var sourceDamage : state.combatDamageDealtToPlayer.entrySet()) {
                if (sourceDamage.getValue() > 0) {
                    gameData.recordDamageDealtBySourceToPlayer(
                            sourceDamage.getKey().getId(), defenderId, sourceDamage.getValue());
                    state.combatDamageRecipientControllers
                            .computeIfAbsent(sourceDamage.getKey(), ignored -> new HashSet<>())
                            .add(defenderId);
                }
            }
            gameData.recordCombatDamageToPlayer(defenderId, damageDealt);
            gameData.recordDamageToPlayer(defenderId, damageDealt, Math.min(damageDealt, artifactDamage));
            triggerCollectionService.checkEnchantedPlayerDealtDamageTriggers(
                    gameData, defenderId, damageDealt);
            for (var sourceDamage : state.combatDamageDealtToPlayer.entrySet()) {
                if (sourceDamage.getValue() > 0) {
                    triggerCollectionService.checkOpponentDealtDamageTriggers(
                            gameData, defenderId, sourceDamage.getKey().getId(), sourceDamage.getValue());
                }
            }
        }
    }

    /**
     * Processes pending redirect damage entries populated by {@link DamagePreventionService}
     * when damage redirect shields (e.g. Vengeful Archon, Divine Deflection) prevent damage.
     * Delegates to the single implementation in {@code DamageSupport} so player and permanent
     * redirect targets behave identically inside and outside combat.
     */
    private void processPendingRedirectDamage(GameData gameData) {
        damageSupport.processPendingRedirectDamage(gameData);
    }

    private void applyPlaneswalkerDamage(GameData gameData, CombatDamageState state) {
        for (var entry : state.damageToPlaneswalkers.entrySet()) {
            UUID pwId = entry.getKey();
            int damage = entry.getValue();
            if (damage <= 0) continue;
            Permanent pw = gameQueryService.findPermanentById(gameData, pwId);
            if (pw == null) continue; // planeswalker/battle may have left battlefield
            if (pw.getCard().hasType(CardType.BATTLE)) {
                gameData.recordDamageToPermanent(pw.getId(), damage);
                pw.setCounterCount(CounterType.DEFENSE, pw.getCounterCount(CounterType.DEFENSE) - damage);
                triggerCollectionService.checkAnyPermanentDealtDamageTriggers(gameData, pw, damage);
                gameLogService.append(gameData, GameLog.cardThen(pw.getCard(), " takes " + damage + " combat damage ("
                        + pw.getCounterCount(CounterType.DEFENSE) + " defense remaining)."));
                battleDefeatSupport.checkAfterDefenseRemoved(gameData, pw);
                continue;
            }
            gameData.recordDamageToPermanent(pw.getId(), damage);
            triggerCollectionService.checkAnyPermanentDealtDamageTriggers(gameData, pw, damage);
            if (gameQueryService.isToughnessAsLoyaltyPermanent(gameData, pw)) {
                pw.setToughnessAsLoyalty(
                        gameQueryService.getToughnessAsLoyalty(pw) - damage);
            } else {
                int loyaltyCounterRemoval = gameQueryService.applyPlaneswalkerLoyaltyDamageReplacement(
                        gameData, pw, damage);
                pw.setCounterCount(CounterType.LOYALTY,
                        pw.getCounterCount(CounterType.LOYALTY) - loyaltyCounterRemoval);
            }
            gameLogService.append(gameData, GameLog.cardThen(pw.getCard(), " takes " + damage + " combat damage ("
                    + (gameQueryService.isToughnessAsLoyaltyPermanent(gameData, pw)
                    ? gameQueryService.getEffectiveToughness(gameData, pw) + " toughness remaining)."
                    : pw.getCounterCount(CounterType.LOYALTY) + " loyalty remaining).")));
        }
    }

    private void processOpponentSourceDamageToYouOrYourPermanentTriggers(GameData gameData,
                                                                           CombatDamageState state) {
        for (var entry : state.combatDamageRecipientControllers.entrySet()) {
            Permanent source = entry.getKey();
            UUID sourceControllerId = state.combatDamageDealerControllers.get(source);
            if (sourceControllerId == null) {
                sourceControllerId = gameQueryService.findPermanentController(gameData, source.getId());
            }
            triggerCollectionService.checkOpponentSourceDamageToYouOrYourPermanentTriggers(
                    gameData, source.getCard(), sourceControllerId, source.getId(), entry.getValue());
        }
    }

    /**
     * Queues the damage redirect effects just took over from combat damage (Harm's Way, Saving Grace,
     * the creatures and Auras that absorb damage): the redirected part is still combat damage of
     * this step (CR 510.2), dealt to its new recipient instead (CR 614.9), so it becomes an event of
     * its own that the new recipient's replacement and prevention effects modify (CR 616.1). Effects
     * that already modified {@code origin} don't apply to it again (CR 614.5).
     */
    private void queueRedirectedCombatDamage(GameData gameData, CombatDamageState state, DamageEvent origin) {
        if (gameData.pendingSourceRedirectDamage.isEmpty()) return;

        List<SourceDamageRedirectShield> toProcess = new ArrayList<>(gameData.pendingSourceRedirectDamage);
        gameData.pendingSourceRedirectDamage.clear();

        for (SourceDamageRedirectShield redirect : toProcess) {
            DamageEvent redirected = redirectedCombatEvent(gameData, origin, redirect);
            if (redirected != null) {
                state.pendingDamageEvents.add(redirected);
            }
        }
    }

    private DamageEvent redirectedCombatEvent(GameData gameData, DamageEvent origin,
                                              SourceDamageRedirectShield redirect) {
        UUID targetId = redirect.redirectTargetId();
        int damage = redirect.remainingAmount();
        if (damage <= 0) return null;
        boolean toPlayer = gameData.playerIds.contains(targetId);
        Permanent target = toPlayer ? null : findOnBattlefield(gameData, targetId);
        if (!toPlayer && target == null) return null;

        List<DamagePortion> portions = new ArrayList<>();
        if (redirect.damageSourceId() == null && origin != null && origin.hasPortions()) {
            // A redirect of the whole event (Palisade Giant) takes every source's part of it.
            for (DamagePortion portion : origin.withAmount(damage).portions()) {
                if (portion.amount() > 0) {
                    portions.add(redirectedPortion(gameData, portion.sourceId(), portion,
                            portion.sourceSnapshot(), portion.amount(), target));
                }
            }
        } else {
            // A per-source rule sees one source's part of the event, as damage from that source alone.
            UUID sourceId = redirect.damageSourceId() != null ? redirect.damageSourceId()
                    : origin == null ? null : origin.entry().getSourcePermanentId();
            if (sourceId == null) return null;
            DamagePortion originPortion = origin == null ? null : origin.portions().stream()
                    .filter(portion -> portion.sourceId().equals(sourceId))
                    .findFirst().orElse(null);
            Permanent originSnapshot = originPortion != null ? originPortion.sourceSnapshot()
                    : origin != null && sourceId.equals(origin.entry().getSourcePermanentId())
                    ? origin.entry().getSourcePermanentSnapshot() : null;
            portions.add(redirectedPortion(gameData, sourceId, originPortion, originSnapshot, damage, target));
        }
        portions.removeIf(Objects::isNull);
        if (portions.isEmpty()) return null;

        gameLogService.append(gameData, toPlayer
                ? GameLog.text(damage + " damage is redirected to " + gameData.playerIdToName.get(targetId) + ".")
                : GameLog.textCardText(damage + " damage is redirected to ", target.getCard(), "."));
        DamageEvent event = combatDamageEvent(gameData, targetId, portions);
        if (event == null) return null;
        Set<String> spentKeys = new HashSet<>();
        if (origin != null) {
            origin.appliedKeys().stream().filter(RedirectedDamageRules::staysSpent).forEach(spentKeys::add);
        }
        return event.asRedirectedCombat(spentKeys);
    }

    /**
     * {@code amount} of {@code sourceId}'s combat damage as dealt to its new recipient: whether it is
     * dealt as counters or poison depends on that recipient, the rest on the source.
     */
    private DamagePortion redirectedPortion(GameData gameData, UUID sourceId, DamagePortion originPortion,
                                            Permanent originSnapshot, int amount, Permanent target) {
        Permanent live = gameQueryService.findPermanentById(gameData, sourceId);
        Permanent snapshot = originSnapshot != null ? originSnapshot : live != null ? new Permanent(live) : null;
        if (snapshot == null) return null;
        Permanent source = live != null ? live : snapshot;
        boolean counterDamage = target == null ? gameQueryService.hasKeyword(gameData, source, Keyword.INFECT)
                : gameQueryService.dealsCounterDamageToCreatures(gameData, source);
        boolean deathtouch = originPortion != null ? originPortion.deathtouch()
                : gameQueryService.hasKeyword(gameData, source, Keyword.DEATHTOUCH);
        boolean unpreventable = gameQueryService.damageCantBePreventedFromSource(gameData, source, true)
                || target != null && target.isDamageCantBePreventedOrRedirectedThisTurn();
        if (target != null && !unpreventable && gameQueryService.isDamagePreventable(gameData, true)
                && gameQueryService.hasProtectionFromDamageSource(gameData, target, source)) {
            // CR 702.16e: protection prevents the redirected damage, but the redirection still happened.
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s damage is prevented."));
            return null;
        }
        return new DamagePortion(sourceId, snapshot, amount, counterDamage, deathtouch,
                originPortion != null && originPortion.unblocked(), unpreventable);
    }

    /**
     * Deals redirected combat damage once its new recipient's effects are applied: life loss or
     * poison for a player, loyalty, defense, counters or marked damage for a permanent, and the
     * records and triggers of that damage.
     */
    private void dealRedirectedCombatDamage(GameData gameData, CombatDamageState state, DamageEvent event) {
        for (DamagePortion portion : event.portions()) {
            if (portion.amount() <= 0) continue;
            UUID sourceId = portion.sourceId();
            Permanent damageSource = gameQueryService.findPermanentById(gameData, sourceId);
            UUID sourceControllerId = gameQueryService.findPermanentController(gameData, sourceId);
            if (!event.toPermanent()) {
                dealRedirectedCombatDamageToPlayer(gameData, state, event.recipientPlayerId(), portion,
                        damageSource, sourceControllerId);
            } else {
                Permanent targetPerm = gameQueryService.findPermanentById(gameData, event.recipientPermanentId());
                if (targetPerm != null) {
                    dealRedirectedCombatDamageToPermanent(gameData, state, targetPerm, portion, damageSource,
                            sourceControllerId);
                }
            }
        }
    }

    private void dealRedirectedCombatDamageToPlayer(GameData gameData, CombatDamageState state, UUID targetId,
                                                    DamagePortion portion, Permanent damageSource,
                                                    UUID sourceControllerId) {
        int damage = portion.amount();
        UUID sourceId = portion.sourceId();
        if (portion.counterDamage()) {
            lifeSupport.applyPoisonCounters(gameData, targetId, damage,
                    portion.sourceSnapshot().getCard().getName(), sourceControllerId);
        } else if (gameQueryService.canPlayerLoseLife(gameData, targetId)) {
            int lifeLoss = damage * gameQueryService.opponentLifeLossMultiplier(gameData, targetId);
            gameData.playerLifeTotals.put(targetId, gameQueryService.lifeAfterDamage(gameData, targetId, lifeLoss));
        }
        gameData.recordCombatDamageToPlayer(targetId, damage);
        if (damageSource != null) {
            state.combatDamageDealt.merge(damageSource, damage, Integer::sum);
            state.redirectedCombatDamageSourcesToPlayers
                    .computeIfAbsent(targetId, ignored -> new LinkedHashSet<>()).add(damageSource);
        }
        boolean artifactSource = damageSource != null && gameQueryService.isArtifact(gameData, damageSource);
        gameData.recordDamageToPlayer(targetId, damage, artifactSource ? damage : 0);
        gameData.recordDamageDealtBySourceToPlayer(sourceId, targetId, damage);
        gameData.recordDamageRecipientBySource(sourceId, targetId);
        if (damageSource != null) {
            gameData.recordCombatDamageByCreatureNameToPlayer(damageSource.getCard().getName(), targetId);
        }
        triggerCollectionService.checkEnchantedPlayerDealtDamageTriggers(gameData, targetId, damage);
        triggerCollectionService.checkOpponentDealtDamageTriggers(gameData, targetId, sourceId, damage);
        if (damageSource != null && sourceControllerId != null && !portion.counterDamage()) {
            checkRedirectedCombatDamageAllyTriggers(gameData, damageSource, sourceControllerId, targetId, damage);
        }
    }

    /**
     * The redirected part of combat damage is still combat damage dealt to a player (CR 510.2,
     * CR 614.9), so "whenever a creature you control deals combat damage to a player" abilities and
     * "whenever your opponents are dealt combat damage" abilities trigger for it.
     */
    private void checkRedirectedCombatDamageAllyTriggers(GameData gameData, Permanent damageSource,
                                                         UUID sourceControllerId, UUID recipientId, int damage) {
        Map<Permanent, Integer> damageBySource = new LinkedHashMap<>();
        damageBySource.put(damageSource, damage);
        Set<UUID> firedBatchedAllyTriggerSources = new HashSet<>();
        checkAllyCreatureCombatDamageToPlayerTriggers(gameData, damageSource, sourceControllerId, recipientId,
                damage, damageBySource, firedBatchedAllyTriggerSources, false, false);
        for (UUID otherPlayerId : gameData.orderedPlayerIds) {
            if (otherPlayerId.equals(sourceControllerId)) continue;
            checkAllyCreatureCombatDamageToPlayerTriggers(gameData, damageSource, otherPlayerId, recipientId,
                    damage, damageBySource, firedBatchedAllyTriggerSources, false, true);
        }
    }

    private void dealRedirectedCombatDamageToPermanent(GameData gameData, CombatDamageState state,
                                                       Permanent targetPerm, DamagePortion portion,
                                                       Permanent damageSource, UUID sourceControllerId) {
        int effectiveDamage = portion.amount();
        UUID sourceId = portion.sourceId();
        damagePreventionService.applyDamageHealingReplacement(gameData, targetPerm, effectiveDamage);
        boolean toughnessAsLoyalty = gameQueryService.isToughnessAsLoyaltyPermanent(gameData, targetPerm);
        if (toughnessAsLoyalty) {
            targetPerm.setToughnessAsLoyalty(gameQueryService.getToughnessAsLoyalty(targetPerm) - effectiveDamage);
        } else if (targetPerm.getCard().hasType(CardType.PLANESWALKER)) {
            int loyaltyCounterRemoval = gameQueryService.applyPlaneswalkerLoyaltyDamageReplacement(
                    gameData, targetPerm, effectiveDamage);
            targetPerm.setCounterCount(CounterType.LOYALTY,
                    targetPerm.getCounterCount(CounterType.LOYALTY) - loyaltyCounterRemoval);
        }
        if (targetPerm.getCard().hasType(CardType.BATTLE)) {
            targetPerm.setCounterCount(CounterType.DEFENSE,
                    targetPerm.getCounterCount(CounterType.DEFENSE) - effectiveDamage);
        }
        boolean isCreature = gameQueryService.isCreature(gameData, targetPerm);
        boolean infect = isCreature && portion.counterDamage();
        if (infect && !gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, targetPerm)) {
            int counters = gameQueryService.reduceMinusOneMinusOneCounters(gameData, targetPerm, effectiveDamage);
            targetPerm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE,
                    targetPerm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + counters);
        }
        if (!infect && ((isCreature && !toughnessAsLoyalty)
                || (!toughnessAsLoyalty && !targetPerm.getCard().hasType(CardType.PLANESWALKER)
                && !targetPerm.getCard().hasType(CardType.BATTLE)))) {
            targetPerm.addMarkedDamage(sourceId, effectiveDamage);
        }
        if (damageSource != null) {
            state.combatDamageDealt.merge(damageSource, effectiveDamage, Integer::sum);
            if (isCreature) {
                recordCombatDamageToCreature(gameData, state, damageSource, sourceControllerId,
                        targetPerm, effectiveDamage);
            }
        }
        if (isCreature && portion.deathtouch()) {
            targetPerm.setDamagedByDeathtouch(true);
        }
        gameData.recordDamageToPermanent(targetPerm.getId(), effectiveDamage, sourceId,
                damageSource == null ? null : gameQueryService.getEffectiveName(gameData, damageSource),
                sourceControllerId);
        if (isCreature) {
            gameData.recordDamageDealtToCreatureBySource(sourceId, targetPerm.getId());
        }
        triggerCollectionService.checkAnyPermanentDealtDamageTriggers(gameData, targetPerm, effectiveDamage);
        if (targetPerm.getCard().hasType(CardType.BATTLE)) {
            battleDefeatSupport.checkAfterDefenseRemoved(gameData, targetPerm);
        }
        recordQualifyingCombatDamageBySourceId(gameData, sourceId, targetPerm);
        gameData.recordDamageRecipientBySource(sourceId, targetPerm.getId());
        gameData.permanentsDealtDamageThisTurn.add(targetPerm.getId());
    }

    /**
     * Processes pending Eye for an Eye reflected damage during combat: deals the reflected amount to
     * the chosen source's controller (a player). Mirrors {@link #processSourceRedirectDamage}'s direct
     * life handling to avoid re-entering combat damage accumulation.
     */
    private void processEyeForAnEyeReflections(GameData gameData) {
        damageSupport.processEyeForAnEyeReflections(gameData);
    }

    private void accumulatePlayerDamage(GameData gameData, Permanent atk, CombatantStats atkStats,
                                         int damage, UUID defenderId,
                                         CombatDamageState state, boolean sourceUnblocked) {
        withSourceUnpreventableDamage(gameData, atk, () -> accumulatePlayerDamageInternal(
                gameData, atk, atkStats, damage, defenderId, state, null, sourceUnblocked));
    }

    private void accumulatePlayerDamageToPlayer(GameData gameData, Permanent atk, CombatantStats atkStats,
                                                int damage, UUID playerId,
                                                CombatDamageState state, boolean sourceUnblocked) {
        withSourceUnpreventableDamage(gameData, atk, () -> accumulatePlayerDamageInternal(
                gameData, atk, atkStats, damage, playerId, state, playerId, sourceUnblocked));
    }

    private void accumulatePlayerDamageInternal(GameData gameData, Permanent atk, CombatantStats atkStats,
                                                 int damage, UUID defenderId,
                                                 CombatDamageState state, UUID forcedPlayerTarget,
                                                 boolean sourceUnblocked) {
        UUID playerTarget = forcedPlayerTarget != null ? forcedPlayerTarget : atk.getAttackTarget();
        if (playerTarget != null && gameData.playerIds.contains(playerTarget)
                && !playerTarget.equals(defenderId)) {
            accumulatePlayerDamageInternal(gameData, atk, atkStats, damage, playerTarget,
                    state, playerTarget, sourceUnblocked);
            return;
        }
        UUID sourceControllerId = gameQueryService.findPermanentController(gameData, atk.getId());
        if (sourceControllerId != null) {
            state.combatDamageDealerControllers.putIfAbsent(atk, sourceControllerId);
        }

        // Check if the attacker is attacking a planeswalker instead of a player
        UUID attackTarget = forcedPlayerTarget != null ? forcedPlayerTarget : atk.getAttackTarget();
        if (attackTarget != null && !gameData.playerIds.contains(attackTarget)) {
            // CR 510.1b: If the planeswalker left the battlefield, the creature assigns no combat damage
            Permanent pw = gameQueryService.findPermanentById(gameData, attackTarget);
            if (pw == null) return;
            if (gameQueryService.isDamagePreventable(gameData, true)
                    && !gameQueryService.damageCantBePreventedFromSource(gameData, atk, true)
                    && !pw.isDamageCantBePreventedOrRedirectedThisTurn()
                    && gameQueryService.hasProtectionFromDamageSource(gameData, pw, atk)) {
                return;
            }
            collectCombatDamage(gameData, state, atk, atkStats, pw.getId(), pw, damage, sourceUnblocked);
            return;
        }

        collectCombatDamage(gameData, state, atk, atkStats, defenderId, null, damage, sourceUnblocked);
    }

    /** Continues the combat damage event a Cleric's "you may prevent" paused. */
    public void completeClericPreventionChoice(GameData gameData,
            com.github.laxika.magicalvibes.model.PendingMayAbility ability, int remainingDamage) {
        CombatDamageState state = gameData.pendingCombatDamageState;
        if (state == null) return;
        var effect = (com.github.laxika.magicalvibes.model.effect.PreventDamageToControllerPerClericEffect)
                ability.effects().getFirst();
        StackEntry damageEntry = new StackEntry(effect.damageEntryType(), ability.sourceCard(),
                ability.sourceControllerId(), "Combat damage", List.of(), effect.damagedPlayerId(),
                ability.sourcePermanentId());
        damageEntry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
        if (damagePreventionService.queueClericPreventionChoice(gameData, damageEntry,
                effect.damagedPlayerId(), remainingDamage, true, effect.remainingAlchemistIds())) return;
        damageSupport.resumeSuspendedCombatDamage(gameData, ability.sourcePermanentId(), remainingDamage);
        continueCombatDamageAfterDecision(gameData);
    }

    private Map<Integer, Integer> precomputeBlockerDamage(DamagePhaseSnapshot snap,
                                                           Map<Integer, List<Integer>> blockerMap,
                                                           Set<Integer> deadDefenderIndices,
                                                           DamagePhase phase) {
        Map<Integer, Integer> blockerDamage = new HashMap<>();
        for (List<Integer> blkList : blockerMap.values()) {
            for (int blkIdx : blkList) {
                CombatantStats blkStats = snap.defenderStats().get(blkIdx);
                if (phase != DamagePhase.REGULAR || !deadDefenderIndices.contains(blkIdx)) {
                    if (blkStats.participatesInDamagePhase(phase)) {
                        blockerDamage.putIfAbsent(blkIdx, blkStats.combatDamage());
                    }
                }
            }
        }
        return blockerDamage;
    }

    /**
     * Whether prevention still applies to combat damage dealt by {@code source}: the snapshotted
     * global answer narrowed by the source's own "damage dealt by this creature can't be prevented"
     * (Malignus).
     */
    private boolean damagePreventableFrom(GameData gameData, boolean snapshotPreventable, Permanent source) {
        return snapshotPreventable && !gameQueryService.damageCantBePreventedFromSource(gameData, source, true);
    }

    /**
     * Runs a damage-dealing action with every prevention path suppressed when the source permanent's
     * damage can't be prevented (Malignus). Reuses the global preventability gate for the duration of
     * the event — the same shape {@code DealDamageToAnyTargetEffectHandler} uses for Banefire — so no
     * prevention site needs to learn about the source.
     */
    private void withSourceUnpreventableDamage(GameData gameData, Permanent source, Runnable action) {
        if (!gameQueryService.damageCantBePreventedFromSource(gameData, source, true)) {
            action.run();
            return;
        }
        boolean previous = gameData.damageCantBePreventedThisTurn;
        gameData.damageCantBePreventedThisTurn = true;
        try {
            action.run();
        } finally {
            gameData.damageCantBePreventedThisTurn = previous;
        }
    }

    /**
     * Collects combat damage {@code source} assigns to the creature {@code target}. Returns whether
     * the damage is taken care of, so the caller must not count it as dealt yet.
     */
    private boolean applyCombatCreatureDamage(GameData gameData, CombatDamageState state,
                                           Permanent source, CombatantStats sourceStats,
                                           Permanent target, int damage) {
        if (!target.isDamageCantBePreventedOrRedirectedThisTurn()
                && isGlobalCreaturePreventionLifeGain(gameData, source)) {
            damagePreventionService.applyAllByCreaturesPreventionLifeGain(gameData,
                    gameQueryService.applyDamageReplacementEffects(gameData, damage),
                    gameQueryService.findPermanentController(gameData, target.getId()));
            return true;
        }
        collectCombatDamage(gameData, state, source, sourceStats, target.getId(), target, damage, false);
        return true;
    }

    /**
     * Offers each player redirecting all damage to creatures the choice to be dealt {@code target}'s
     * combat damage instead; the event waits for the answers.
     */
    public void queueCombatCreatureRedirectChoice(GameData gameData, Permanent source, Permanent target,
                                                  int damage, List<UUID> controllers) {
        UUID sourceController = gameQueryService.findPermanentController(gameData, source.getId());
        gameData.pendingMayAbilities.addFirst(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                source.getCard(), controllers.getFirst(), List.of(
                new com.github.laxika.magicalvibes.model.effect.RedirectAllCreatureDamageToControllerEffect(
                        controllers.subList(1, controllers.size()), true, -1, false,
                        false, StackEntryType.ACTIVATED_ABILITY)),
                "Have " + damage + " combat damage dealt to you instead?", target.getId(), null,
                source.getId(), null, 0, 0, null, null, null, new Permanent(source), sourceController, null, damage));
    }

    /**
     * Completes one player's answer to the all-creature-damage redirect: the next player is asked,
     * the damage is dealt to the accepting player, or the paused event continues to the creature.
     */
    public void completeCreatureDamageRedirectChoice(GameData gameData,
            com.github.laxika.magicalvibes.model.PendingMayAbility ability, boolean accepted) {
        CombatDamageState state = gameData.pendingCombatDamageState;
        if (state == null) return;
        var effect = (com.github.laxika.magicalvibes.model.effect.RedirectAllCreatureDamageToControllerEffect)
                ability.effects().getFirst();
        Permanent source = gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());
        if (source == null) source = ability.sourcePermanentSnapshot();
        Permanent target = gameQueryService.findPermanentById(gameData, ability.targetCardId());
        if (!accepted && !effect.remainingControllers().isEmpty() && source != null && target != null) {
            queueCombatCreatureRedirectChoice(gameData, source, target, ability.eventValue(),
                    effect.remainingControllers());
            return;
        }
        if (accepted) {
            // The whole paused event is redirected, so each source keeps its own part of the damage.
            DamageEvent paused = gameData.suspendedReplacementEvent instanceof DamageEvent event && event.combat()
                    ? event : null;
            gameData.suspendedReplacementEvent = null;
            if (paused != null && paused.hasPortions()) {
                gameData.pendingSourceRedirectDamage.add(new SourceDamageRedirectShield(
                        ability.controllerId(), null, ability.eventValue(), ability.controllerId()));
                queueRedirectedCombatDamage(gameData, state, paused);
            } else if (source != null) {
                gameData.pendingSourceRedirectDamage.add(new SourceDamageRedirectShield(
                        ability.controllerId(), source.getId(), ability.eventValue(), ability.controllerId()));
                queueRedirectedCombatDamage(gameData, state, paused);
            }
        } else {
            damageSupport.resumeSuspendedCombatDamage(gameData, null, -1);
        }
        continueCombatDamageAfterDecision(gameData);
    }

    /**
     * Collects {@code amount} combat damage {@code source} deals to {@code recipientId} in this
     * step. All of a step's combat damage is dealt at once (CR 510.2), so once every source has
     * assigned its damage each recipient's damage becomes one event (see
     * {@link #processCollectedCombatDamage}).
     */
    private void collectCombatDamage(GameData gameData, CombatDamageState state, Permanent source,
                                     CombatantStats sourceStats, UUID recipientId, Permanent recipient,
                                     int amount, boolean unblocked) {
        if (amount <= 0) {
            state.combatDamageDealt.merge(source, 0, Integer::sum);
            return;
        }
        boolean counterDamage = recipient == null ? sourceStats.infect()
                : gameQueryService.dealsCounterDamageToCreatures(gameData, source);
        boolean unpreventable = gameQueryService.damageCantBePreventedFromSource(gameData, source, true)
                || (recipient != null && recipient.isDamageCantBePreventedOrRedirectedThisTurn());
        List<DamagePortion> portions = state.collectedPortions.computeIfAbsent(recipientId, ignored -> new ArrayList<>());
        for (int i = 0; i < portions.size(); i++) {
            DamagePortion existing = portions.get(i);
            if (existing.sourceId().equals(source.getId())) {
                portions.set(i, existing.withAmount(existing.amount() + amount));
                return;
            }
        }
        portions.add(new DamagePortion(source.getId(), source, amount, counterDamage, sourceStats.deathtouch(),
                unblocked, unpreventable));
    }

    /**
     * Turns the damage collected in this step into one event per recipient and applies each one's
     * replacement and prevention effects. Players make those choices in APNAP order (CR 616.1,
     * CR 101.4); an event that waits for a choice parks the step until it is made.
     */
    private void processCollectedCombatDamage(GameData gameData, CombatDamageState state) {
        List<DamageEvent> events = new ArrayList<>();
        state.collectedPortions.forEach((recipientId, portions) -> {
            DamageEvent event = combatDamageEvent(gameData, recipientId, portions);
            if (event != null) events.add(event);
        });
        state.collectedPortions.clear();
        List<UUID> apnap = new ArrayList<>();
        int activeIndex = Math.max(0, gameData.orderedPlayerIds.indexOf(gameData.activePlayerId));
        for (int i = 0; i < gameData.orderedPlayerIds.size(); i++) {
            apnap.add(gameData.orderedPlayerIds.get((activeIndex + i) % gameData.orderedPlayerIds.size()));
        }
        events.sort(Comparator.comparingInt(event -> {
            int index = apnap.indexOf(event.chooserId());
            return index < 0 ? apnap.size() : index;
        }));
        state.pendingDamageEvents.addAll(events);
        runPendingCombatDamageEvents(gameData, state);
    }

    private DamageEvent combatDamageEvent(GameData gameData, UUID recipientId, List<DamagePortion> portions) {
        if (portions.isEmpty()) return null;
        boolean playerRecipient = gameData.playerIds.contains(recipientId);
        Permanent recipient = playerRecipient ? null : findOnBattlefield(gameData, recipientId);
        if (!playerRecipient && recipient == null) return null;
        DamagePortion first = portions.getFirst();
        UUID sourceControllerId = gameQueryService.findPermanentController(gameData, first.sourceId());
        StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, first.sourceSnapshot().getCard(),
                sourceControllerId != null ? sourceControllerId : gameData.activePlayerId, "Combat damage",
                List.of(), recipientId, first.sourceId());
        entry.setSourcePermanentSnapshot(new Permanent(first.sourceSnapshot()));
        UUID chooserId = playerRecipient ? recipientId : controllerOnBattlefield(gameData, recipientId);
        return DamageEvent.combat(entry, chooserId, playerRecipient ? null : recipientId,
                portions.stream().allMatch(DamagePortion::unpreventable),
                recipient != null && recipient.isDamageCantBePreventedOrRedirectedThisTurn(), portions);
    }

    /** Runs the step's waiting damage events until one of them waits for a choice. */
    private void runPendingCombatDamageEvents(GameData gameData, CombatDamageState state) {
        CombatDamageState previous = gameData.combatDamageStateInProgress;
        gameData.combatDamageStateInProgress = state;
        try {
            while (!state.pendingDamageEvents.isEmpty()) {
                DamageEvent event = state.pendingDamageEvents.removeFirst();
                restoreCombatShields(gameData, state, event);
                damageSupport.runCombatDamageEvent(gameData, event);
                processPendingRedirectDamage(gameData);
                if (damageSupport.isReplacementDecisionOutstanding(gameData)) {
                    state.pendingOptionalDamageChoices++;
                    return;
                }
            }
        } finally {
            gameData.combatDamageStateInProgress = previous;
        }
    }

    /**
     * Continues the parked combat damage step once the choice one of its events waited for is made
     * and that event is dealt.
     */
    public void continueCombatDamageAfterDecision(GameData gameData) {
        CombatDamageState state = gameData.pendingCombatDamageState;
        if (state == null || damageSupport.isReplacementDecisionOutstanding(gameData)) return;
        if (state.pendingOptionalDamageChoices > 0) {
            state.pendingOptionalDamageChoices--;
        }
        runPendingCombatDamageEvents(gameData, state);
    }

    /**
     * One-shot shields for "the next damage" from a source or to a recipient see every event of the
     * step they apply to: all of a step's combat damage is one simultaneous dealing (CR 510.2).
     */
    private void restoreCombatShields(GameData gameData, CombatDamageState state, DamageEvent event) {
        boolean playerRecipient = !event.toPermanent();
        UUID recipientId = playerRecipient ? event.recipientPlayerId() : event.recipientPermanentId();
        restoreSharedRedirectForCombatChunk(gameData, state, recipientId, playerRecipient);
        for (DamagePortion portion : event.portions()) {
            restoreSourceShieldForCombatChunk(gameData, state, portion.sourceId(), recipientId, playerRecipient);
        }
    }

    private CombatDamageState activeCombatDamageState(GameData gameData) {
        return gameData.combatDamageStateInProgress != null
                ? gameData.combatDamageStateInProgress : gameData.pendingCombatDamageState;
    }

    /**
     * Deals the damage redirect effects just took over from {@code origin}'s combat damage: queued
     * as events of the step in progress, or, without one, as damage of its own.
     */
    public void processCombatRedirects(GameData gameData, DamageEvent origin) {
        CombatDamageState state = activeCombatDamageState(gameData);
        if (state == null) {
            damageSupport.processSourceRedirectDamage(gameData);
            return;
        }
        queueRedirectedCombatDamage(gameData, state, origin);
    }

    /** Dralnu's sacrifices for combat damage happen once the step's damage is dealt. */
    public void queueDralnuReplacement(GameData gameData, Permanent target, int damage) {
        CombatDamageState state = activeCombatDamageState(gameData);
        if (state == null) {
            damageSupport.applyDralnuReplacement(gameData, target, damage);
            return;
        }
        queueDralnuReplacement(state, target, damage);
    }

    /** A creature a prevention effect shuffles away leaves once the step's damage is dealt. */
    public void queueShuffleAfterCombatDamage(GameData gameData, Permanent target) {
        CombatDamageState state = activeCombatDamageState(gameData);
        if (state == null) {
            permanentRemovalService.removePermanentToLibraryShuffled(gameData, target);
            return;
        }
        state.permanentsToShuffleAfterDamage.put(target.getId(), target);
    }

    /** Whether an effect like Undead Alchemist's replaces {@code source}'s combat damage to a player with milling. */
    public boolean hasCombatDamageMillReplacement(GameData gameData, Permanent source) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, source.getId());
        return controllerId != null && findCombatDamageMillReplacement(gameData, controllerId, source) != null;
    }

    /** The player mills that many cards instead of being dealt {@code source}'s combat damage. */
    public void replaceCombatDamageWithMill(GameData gameData, Permanent source, UUID playerId, int damage) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, source.getId());
        ReplaceCombatDamageWithMillEffect replacement = controllerId == null
                ? null : findCombatDamageMillReplacement(gameData, controllerId, source);
        if (replacement == null) return;
        gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                "'s " + damage + " combat damage is replaced with milling."));
        if (replacement.counterType() != null) {
            permanentCounterSupport.placeCounterOnPermanent(gameData, null, source, replacement.counterType(), damage);
        }
        graveyardService.resolveMillPlayer(gameData, playerId, damage);
    }

    /**
     * Records a combat damage event once its replacement and prevention effects are applied, for
     * the step to deal with the rest of its damage (CR 510.2).
     */
    public void recordCombatDamageEvent(GameData gameData, DamageEvent event) {
        CombatDamageState state = activeCombatDamageState(gameData);
        if (state == null) return;
        if (event.redirected()) {
            dealRedirectedCombatDamage(gameData, state, event);
            return;
        }
        if (!event.toPermanent()) {
            recordCombatEventToPlayer(gameData, state, event);
            return;
        }
        Permanent target = findOnBattlefield(gameData, event.recipientPermanentId());
        if (target == null) return;
        if (!gameQueryService.isCreature(gameData, target)
                && (gameQueryService.isPlaneswalker(gameData, target) || target.getCard().hasType(CardType.BATTLE))) {
            recordCombatEventToAttackedPermanent(gameData, state, event, target);
        } else {
            recordCombatEventToCreature(gameData, state, event, target);
        }
    }

    private static Permanent findOnBattlefield(GameData gameData, UUID permanentId) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (permanent.getId().equals(permanentId)) return permanent;
            }
        }
        return null;
    }

    private static UUID controllerOnBattlefield(GameData gameData, UUID permanentId) {
        for (var entry : gameData.playerBattlefields.entrySet()) {
            for (Permanent permanent : entry.getValue()) {
                if (permanent.getId().equals(permanentId)) return entry.getKey();
            }
        }
        return null;
    }

    private Permanent combatSource(GameData gameData, DamagePortion portion) {
        Permanent live = gameQueryService.findPermanentById(gameData, portion.sourceId());
        return live != null ? live : portion.sourceSnapshot();
    }

    private void recordCombatEventToPlayer(GameData gameData, CombatDamageState state, DamageEvent event) {
        UUID playerId = event.recipientPlayerId();
        for (DamagePortion portion : event.portions()) {
            Permanent source = combatSource(gameData, portion);
            int amount = portion.amount();
            state.combatDamageDealt.merge(source, amount, Integer::sum);
            state.combatDamageDealtToPlayer.merge(source, amount, Integer::sum);
            state.combatDamagePlayerRecipients.put(source, playerId);
            if (amount <= 0) continue;
            if (portion.counterDamage()) {
                state.poisonDamageToDefendingPlayer += amount;
                state.infectDamageToPlayers.merge(playerId, amount, Integer::sum);
            } else {
                state.damageToDefendingPlayer += amount;
                state.normalDamageToPlayers.merge(playerId, amount, Integer::sum);
                if (portion.unpreventable()) {
                    state.unpreventableDamageToDefendingPlayer += amount;
                    state.unpreventableDamageToPlayers.merge(playerId, amount, Integer::sum);
                }
            }
        }
    }

    private void recordCombatEventToAttackedPermanent(GameData gameData, CombatDamageState state, DamageEvent event,
                                                      Permanent target) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        for (DamagePortion portion : event.portions()) {
            Permanent source = combatSource(gameData, portion);
            int amount = portion.amount();
            state.damageToPlaneswalkers.merge(target.getId(), amount, Integer::sum);
            state.combatDamageDealtToPlaneswalker.merge(source, amount, Integer::sum);
            state.combatDamageDealt.merge(source, amount, Integer::sum);
            if (amount <= 0) continue;
            if (target.getCard().hasType(CardType.BATTLE)) {
                state.combatDamageAmountsToBattles
                        .computeIfAbsent(source, ignored -> new HashMap<>())
                        .merge(target.getId(), amount, Integer::sum);
            }
            recordQualifyingCombatDamage(gameData, source, target);
            state.combatDamageAmountsToPlaneswalkers
                    .computeIfAbsent(source, ignored -> new HashMap<>())
                    .merge(target.getId(), amount, Integer::sum);
            if (controllerId != null) {
                state.combatDamageRecipientControllers
                        .computeIfAbsent(source, ignored -> new HashSet<>())
                        .add(controllerId);
            }
            gameData.recordDamageRecipientBySource(source.getId(), target.getId());
        }
    }

    private void recordCombatEventToCreature(GameData gameData, CombatDamageState state, DamageEvent event,
                                             Permanent target) {
        List<Permanent> attackers = gameData.playerBattlefields.getOrDefault(gameData.activePlayerId, List.of());
        int attackerIndex = attackers.indexOf(target);
        boolean attackerSide = attackerIndex >= 0;
        int index = attackerSide ? attackerIndex : gameData.playerBattlefields.getOrDefault(
                gameQueryService.getOpponentId(gameData, gameData.activePlayerId), List.of()).indexOf(target);
        Map<Integer, Integer> damageTaken = attackerSide ? state.atkDamageTaken : state.defDamageTaken;
        Map<Integer, Integer> unpreventableTaken = attackerSide
                ? state.unpreventableAtkDamageTaken : state.unpreventableDefDamageTaken;
        Set<Integer> deathtouchIndices = attackerSide
                ? state.deathtouchDamagedAttackerIndices : state.deathtouchDamagedDefenderIndices;
        Map<Integer, Map<UUID, Integer>> takenBySource = attackerSide
                ? state.atkDamageTakenBySource : state.defDamageTakenBySource;
        for (DamagePortion portion : event.portions()) {
            int amount = portion.amount();
            if (amount <= 0) continue;
            Permanent source = combatSource(gameData, portion);
            UUID sourceControllerId = state.combatDamageDealerControllers.get(source);
            if (sourceControllerId == null) {
                sourceControllerId = gameQueryService.findPermanentController(gameData, source.getId());
            }
            if (portion.counterDamage()) {
                recordCounterCombatDamage(gameData, source, sourceControllerId, portion, target);
            } else if (index >= 0) {
                damageTaken.merge(index, amount, Integer::sum);
                takenBySource.computeIfAbsent(index, ignored -> new HashMap<>())
                        .merge(source.getId(), amount, Integer::sum);
                if (portion.unpreventable()) {
                    unpreventableTaken.merge(index, amount, Integer::sum);
                }
                if (portion.deathtouch()) {
                    deathtouchIndices.add(index);
                }
            }
            state.combatDamageDealt.merge(source, amount, Integer::sum);
            recordCombatDamageToCreature(gameData, state, source, sourceControllerId, target, amount);
        }
    }

    /** Infect and wither combat damage to a creature: -1/-1 counters instead of marked damage (CR 120.3d). */
    private void recordCounterCombatDamage(GameData gameData, Permanent source, UUID sourceControllerId,
                                           DamagePortion portion, Permanent target) {
        int amount = portion.amount();
        damagePreventionService.applyDamageHealingReplacement(gameData, target, amount);
        if (!gameQueryService.cantHaveCounters(gameData, target)
                && !gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, target)) {
            // Vizier of Remedies reduces the -1/-1 counters; the deathtouch marking below still
            // keys off the full damage dealt.
            int counters = gameQueryService.reduceMinusOneMinusOneCounters(gameData, target, amount);
            if (counters > 0) {
                target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE,
                        target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + counters);
            }
        }
        gameData.recordDamageToPermanent(target.getId(), amount, source.getId(),
                gameQueryService.getEffectiveName(gameData, source), sourceControllerId);
        triggerCollectionService.checkAnyPermanentDealtDamageTriggers(gameData, target, amount);
        recordQualifyingCombatDamage(gameData, source, target);
        graveyardService.recordCreatureDamagedByPermanent(gameData, source.getId(), target, amount);
        // Damage dealt as counters is still damage from a deathtouch source (CR 702.2b), so the
        // creature is marked for destruction directly.
        if (portion.deathtouch()) {
            target.setDamagedByDeathtouch(true);
        }
    }

    private boolean isGlobalCreaturePreventionLifeGain(GameData gameData, Permanent source) {
        return gameData.preventAllDamageByCreatures
                && !gameData.damageByCreaturesPreventionLifeGainPlayers.isEmpty()
                && gameQueryService.isDamagePreventable(gameData, true)
                && gameQueryService.isCreature(gameData, source)
                && !gameQueryService.damageCantBePreventedFromSource(gameData, source);
    }

    private void recordQualifyingCombatDamage(GameData gameData, Permanent source, Permanent target) {
        if (source == null || target == null || !gameQueryService.isCreature(gameData, source)) return;
        Set<CardSubtype> subtypes = gameQueryService.effectiveCreatureSubtypes(gameData, source);
        if (!subtypes.contains(CardSubtype.GIANT) && !subtypes.contains(CardSubtype.WIZARD)) return;
        UUID controllerId = gameQueryService.findPermanentController(gameData, source.getId());
        if (controllerId != null) {
            gameData.recordQualifyingDamageControllerToPermanent(target.getId(), controllerId);
        }
    }

    private void recordQualifyingCombatDamageBySourceId(GameData gameData, UUID sourceId,
                                                        Permanent target) {
        if (sourceId == null) return;
        recordQualifyingCombatDamage(gameData, gameQueryService.findPermanentById(gameData, sourceId), target);
    }

    private void recordCombatDamageToCreature(GameData gameData, CombatDamageState state,
                                               Permanent source, UUID controllerId,
                                               Permanent target, int damage) {
        if (damage <= 0) return;
        if (source.isAttacking() && target.isBlocking()) {
            state.combatDamageToBlockingCreatureSources.add(source.getId());
        }
        gameData.combatDamageSourcesThatDealtToCreaturesThisTurn.add(source.getId());
        state.combatDamageDealerControllers.putIfAbsent(source, controllerId);
        state.combatDamageDealtToCreatures.computeIfAbsent(source, ignored -> new ArrayList<>()).add(target.getId());
        state.combatDamageAmountsToCreatures
                .computeIfAbsent(source, ignored -> new HashMap<>())
                .merge(target.getId(), damage, Integer::sum);
        // Capture the damaged creature's controller while it is still alive (for reflection triggers).
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (targetControllerId != null) {
            state.combatDamageTargetControllers.putIfAbsent(target.getId(), targetControllerId);
            state.combatDamageRecipientControllers
                    .computeIfAbsent(source, ignored -> new HashSet<>())
                    .add(targetControllerId);
        }
        triggerCollectionService.checkDelayedWatchedCreatureDealtDamageByAttackingCreatureTriggers(
                gameData, source, target, damage);
        triggerCollectionService.checkEnchantedCreatureDealtDamageTriggers(gameData, target, damage);
    }

    private boolean hasFirstOrDoubleStrike(GameData gameData, Permanent creature) {
        return gameQueryService.hasKeyword(gameData, creature, Keyword.FIRST_STRIKE)
                || gameQueryService.hasKeyword(gameData, creature, Keyword.DOUBLE_STRIKE);
    }

    private boolean hasFirstestStrike(GameData gameData, Permanent creature) {
        return gameQueryService.hasActiveStaticEffect(gameData, creature, FirstestStrikeEffect.class);
    }

    /**
     * Finds a combat-damage mill replacement on the controller's battlefield whose predicate
     * matches the attacker.
     */
    private ReplaceCombatDamageWithMillEffect findCombatDamageMillReplacement(
            GameData gameData, UUID controllerId, Permanent attacker) {
        List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
        if (bf == null) return null;
        for (Permanent perm : bf) {
            if (perm.isFaceDown() || gameQueryService.hasLostAllAbilities(gameData, perm)) {
                continue;
            }
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof ReplaceCombatDamageWithMillEffect replacement
                        && predicateEvaluationService.matchesPermanentPredicate(
                        attacker,
                        replacement.attackerPredicate(),
                        FilterContext.of(gameData)
                                .withSourceCardId(perm.getOriginalCard().getId())
                                .withSourceControllerId(controllerId)
                                .withSourcePermanentId(perm.getId()))) {
                    return replacement;
                }
            }
        }
        return null;
    }

    private boolean assignsCombatDamageAsThoughUnblocked(GameData gameData, Permanent attacker) {
        return combatDamageAssignmentEffects(gameData, attacker).stream()
                .anyMatch(AssignCombatDamageAsThoughUnblockedEffect.class::isInstance);
    }

    private boolean mustAssignCombatDamageAsThoughUnblocked(GameData gameData, Permanent attacker) {
        return combatDamageAssignmentEffects(gameData, attacker).stream()
                .filter(AssignCombatDamageAsThoughUnblockedEffect.class::isInstance)
                .map(AssignCombatDamageAsThoughUnblockedEffect.class::cast)
                .anyMatch(AssignCombatDamageAsThoughUnblockedEffect::mandatory);
    }

    private boolean assignsCombatDamageAmongDefendingPlayerAndCreatures(GameData gameData,
                                                                         Permanent attacker) {
        return combatDamageAssignmentEffects(gameData, attacker).stream()
                .anyMatch(CombatDamageAssignmentToDefendingPlayerAndCreaturesEffect.class::isInstance);
    }

    private List<CardEffect> combatDamageAssignmentEffects(GameData gameData, Permanent attacker) {
        List<CardEffect> effects = new ArrayList<>(attacker.getCard().getEffects(EffectSlot.STATIC));
        effects.addAll(attacker.getTemporaryTriggeredEffects(EffectSlot.STATIC));
        effects.addAll(gameQueryService.getGrantedEffects(gameData, attacker));
        UUID attackerControllerId = gameData.findControllerOf(attacker);
        if (attackerControllerId != null) {
            effects.addAll(gameData.playerStaticEffectsUntilEndOfTurn.getOrDefault(attackerControllerId, List.of()));
        }
        return effects;
    }

    private boolean assignsUnblockedDamageToDefendingCreature(GameData gameData, Permanent attacker) {
        return combatDamageAssignmentEffects(gameData, attacker).stream()
                .anyMatch(AssignCombatDamageToDefendingCreatureWhenUnblockedEffect.class::isInstance);
    }

    /**
     * Returns {@code true} if the unblocked attacker may redirect its combat damage to a
     * defending creature (e.g. Cunning Giant) and the defending player controls at least one
     * creature to redirect to, including when attacking that player's planeswalker.
     */
    private boolean canRedirectUnblockedDamageToDefendingCreature(GameData gameData, Permanent atk, List<Permanent> defBf) {
        if (!assignsUnblockedDamageToDefendingCreature(gameData, atk)) return false;
        if (defBf == null) return false;
        for (Permanent p : defBf) {
            if (gameQueryService.isCreature(gameData, p)) return true;
        }
        return false;
    }

    /**
     * Collects the damage-assignment prompts the given damage step needs (CR 510.1c-d):
     * attackers whose damage division involves a choice (multiple blockers, trample or
     * assign-as-unblocked overflow, unblocked redirect) and defending creatures blocking two or
     * more attackers, whose division belongs to the defending player. Fills the pending-index
     * lists on {@link GameData} and reports whether any prompt is needed.
     */
    private boolean collectManualAssignments(GameData gameData, CombatDamageState state,
                                             Map<Integer, List<Integer>> blockerMap,
                                             List<Permanent> atkBf, List<Permanent> defBf,
                                             DamagePhase phase) {
        record Collected(List<Integer> attackerIndices, List<Integer> blockerIndices) {}
        Collected collected = gameQueryService.withQueryScope(gameData, () -> {
            List<Integer> attackers = new ArrayList<>();
            Map<Integer, Integer> livingAttackersPerBlocker = new LinkedHashMap<>();
            for (var bEntry : blockerMap.entrySet()) {
                int bAtkIdx = bEntry.getKey();
                if (phase == DamagePhase.REGULAR && state.deadAttackerIndices.contains(bAtkIdx)) continue;
                Permanent bAtk = atkBf.get(bAtkIdx);
                for (int blkIdx : bEntry.getValue()) {
                    livingAttackersPerBlocker.merge(blkIdx, 1, Integer::sum);
                }
                boolean atkFirstStrike = gameQueryService.hasKeyword(gameData, bAtk, Keyword.FIRST_STRIKE);
                boolean atkDoubleStrike = gameQueryService.hasKeyword(gameData, bAtk, Keyword.DOUBLE_STRIKE);
                boolean participates;
                if (phase == DamagePhase.FIRSTEST_STRIKE) {
                    participates = hasFirstestStrike(gameData, bAtk);
                } else {
                    participates = phase == DamagePhase.FIRST_STRIKE
                            ? (atkFirstStrike || atkDoubleStrike)
                            : (!atkFirstStrike && !hasFirstestStrike(gameData, bAtk) || atkDoubleStrike);
                }
                if (!participates) continue;
                if (gameQueryService.isPreventedFromDealingDamage(gameData, bAtk, true)) continue;
                List<Integer> livingBlockers = new ArrayList<>();
                for (int i : bEntry.getValue()) {
                    if (!state.deadDefenderIndices.contains(i)) livingBlockers.add(i);
                }
                if (phase != DamagePhase.FIRSTEST_STRIKE
                        && needsManualDamageAssignment(gameData, bAtk, livingBlockers, defBf)
                        || phase == DamagePhase.FIRSTEST_STRIKE
                        && !livingBlockers.isEmpty()
                        && needsManualDamageAssignment(gameData, bAtk, livingBlockers, defBf)) {
                    attackers.add(bAtkIdx);
                }
            }

            // CR 510.1d — a creature blocking 2+ attackers has its combat damage divided by the
            // defending player.
            List<Integer> blockers = new ArrayList<>();
            for (var countEntry : livingAttackersPerBlocker.entrySet()) {
                int blkIdx = countEntry.getKey();
                if (countEntry.getValue() < 2
                        && !assignsCombatDamageAmongDefendingPlayerAndCreatures(gameData, defBf.get(blkIdx))
                        && !gameQueryService.hasKeyword(gameData, defBf.get(blkIdx), Keyword.TANTRUM)) continue;
                if (state.deadDefenderIndices.contains(blkIdx)) continue;
                Permanent blk = defBf.get(blkIdx);
                boolean blkFirstStrike = gameQueryService.hasKeyword(gameData, blk, Keyword.FIRST_STRIKE);
                boolean blkDoubleStrike = gameQueryService.hasKeyword(gameData, blk, Keyword.DOUBLE_STRIKE);
                boolean participates = phase == DamagePhase.FIRSTEST_STRIKE
                        ? hasFirstestStrike(gameData, blk)
                        : phase == DamagePhase.FIRST_STRIKE
                        ? (blkFirstStrike || blkDoubleStrike)
                        : (!blkFirstStrike && !hasFirstestStrike(gameData, blk) || blkDoubleStrike);
                if (!participates) continue;
                if (gameQueryService.isPreventedFromDealingDamage(gameData, blk, true)) continue;
                if (gameQueryService.getEffectiveCombatDamage(gameData, blk) <= 0) continue;
                blockers.add(blkIdx);
            }
            return new Collected(attackers, blockers);
        });
        gameData.combatDamagePendingIndices.addAll(collected.attackerIndices());
        gameData.combatDamagePendingBlockerIndices.addAll(collected.blockerIndices());
        return !collected.attackerIndices().isEmpty() || !collected.blockerIndices().isEmpty();
    }

    private boolean needsManualDamageAssignment(GameData gameData, Permanent atk,
                                                List<Integer> livingBlockerIndices,
                                                List<Permanent> defBf) {
        // A creature with 0 or negative power deals no combat damage (CR 510.1a),
        // so there is nothing for the player to distribute.
        if (gameData.creaturesAssigningNoCombatDamageThisTurn.contains(atk.getId())
                || gameData.creaturesAssigningNoCombatDamageThisCombat.contains(atk.getId())
                || gameQueryService.getEffectiveCombatDamage(gameData, atk) <= 0) return false;
        // Blocked with no blockers (CR 510.1c): no damage is assigned, so nothing to divide.
        // A creature that may assign its damage as though it weren't blocked still gets the choice.
        if (livingBlockerIndices.isEmpty() && atk.isBlockedWithoutBlockers()) {
            return assignsCombatDamageAsThoughUnblocked(gameData, atk)
                    && !mustAssignCombatDamageAsThoughUnblocked(gameData, atk);
        }
        if (livingBlockerIndices.isEmpty()) {
            // Unblocked attacker that may assign its combat damage to a defending creature
            // (e.g. Cunning Giant). Prompt only when there is a defending creature to choose.
            boolean attackingPlaneswalker = atk.getAttackTarget() != null
                    && !gameData.playerIds.contains(atk.getAttackTarget());
            return canRedirectUnblockedDamageToDefendingCreature(gameData, atk, defBf)
                    || (!atk.isBlockedWithoutBlockers()
                    && assignsCombatDamageAmongDefendingPlayerAndCreatures(gameData, atk)
                    && (attackingPlaneswalker || defBf.stream()
                    .anyMatch(permanent -> gameQueryService.isCreature(gameData, permanent))));
        }
        if (mustAssignCombatDamageAsThoughUnblocked(gameData, atk)) return false;
        if (assignsCombatDamageAmongDefendingPlayerAndCreatures(gameData, atk)) return true;
        if (livingBlockerIndices.size() >= 2) return true;
        if (gameQueryService.hasKeyword(gameData, atk, Keyword.TRAMPLE)) return true;
        if (assignsCombatDamageAsThoughUnblocked(gameData, atk)) return true;
        return false;
    }

    /**
     * CR 702.22j — the player who assigns a blocked attacker's combat damage among its blockers.
     * Normally the active player (CR 510.1c), but the defending player instead when any of the
     * attacker's living blockers has banding or forms a named bands-with-other pair.
     */
    private UUID attackerDamageAssigner(GameData gameData, Permanent attacker, List<Integer> livingBlockers,
                                        List<Permanent> defBf, UUID activeId, UUID defenderId) {
        if (defBf == null) {
            return activeId;
        }
        if (defendingPlayerAssignsCombatDamage(gameData, attacker, defBf, defenderId)) {
            return defenderId;
        }
        for (int blkIdx : livingBlockers) {
            if (gameQueryService.hasKeyword(gameData, defBf.get(blkIdx), Keyword.BANDING)) {
                return defenderId;
            }
        }
        List<Permanent> blockers = livingBlockers.stream().map(defBf::get).toList();
        if (containsBandsWithOtherPair(gameData, blockers)) {
            return defenderId;
        }
        return activeId;
    }

    private boolean defendingPlayerAssignsCombatDamage(GameData gameData, Permanent attacker, List<Permanent> defBf,
                                                        UUID defenderId) {
        UUID attackTarget = attacker.getAttackTarget();
        if (attackTarget != null && !attackTarget.equals(defenderId)) {
            return false;
        }
        for (Permanent permanent : defBf) {
            if (gameQueryService.hasActiveStaticEffect(gameData, permanent,
                    DefendingPlayerAssignsCombatDamageEffect.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * CR 702.22k — the player who divides a blocking creature's combat damage among the creatures it
     * blocks. Normally the defending player (CR 510.1d), but the active player instead when the
     * blocker is blocking any attacker with banding or a named bands-with-other pair.
     */
    private UUID blockerDamageAssigner(GameData gameData, int blkIdx, CombatDamagePhase1State p1,
                                       List<Permanent> atkBf, UUID activeId, UUID defenderId) {
        List<Permanent> blockedAttackers = new ArrayList<>();
        for (var bEntry : p1.blockerMap.entrySet()) {
            int atkIdx = bEntry.getKey();
            if (p1.deadAttackerIndices.contains(atkIdx)) continue;
            if (!bEntry.getValue().contains(blkIdx)) continue;
            blockedAttackers.add(atkBf.get(atkIdx));
            if (gameQueryService.hasKeyword(gameData, atkBf.get(atkIdx), Keyword.BANDING)) {
                return activeId;
            }
        }
        if (containsBandsWithOtherPair(gameData, blockedAttackers)) {
            return activeId;
        }
        return defenderId;
    }

    private boolean containsBandsWithOtherPair(GameData gameData, List<Permanent> creatures) {
        for (int first = 0; first < creatures.size(); first++) {
            for (int second = first + 1; second < creatures.size(); second++) {
                if (gameQueryService.canUseBandsWithOther(gameData,
                        List.of(creatures.get(first), creatures.get(second)))) return true;
            }
        }
        return false;
    }

    private void restorePhase1State(GameData gameData, CombatDamageState state) {
        CombatDamagePhase1State p1 = gameData.combatDamagePhase1State;
        state.deadAttackerIndices.addAll(p1.deadAttackerIndices);
        state.deadDefenderIndices.addAll(p1.deadDefenderIndices);
        state.atkDamageTaken.putAll(p1.atkDamageTaken);
        state.defDamageTaken.putAll(p1.defDamageTaken);
        state.damageToDefendingPlayer = p1.damageToDefendingPlayer;
        state.unpreventableDamageToDefendingPlayer = p1.unpreventableDamageToDefendingPlayer;
        state.unpreventableAtkDamageTaken.putAll(p1.unpreventableAtkDamageTaken);
        state.unpreventableDefDamageTaken.putAll(p1.unpreventableDefDamageTaken);
        state.damageRedirectedToGuard = p1.damageRedirectedToGuard;
        state.deathtouchDamageRedirectedToGuard = p1.deathtouchDamageRedirectedToGuard;
        if (p1.damageToPlaneswalkers != null) {
            state.damageToPlaneswalkers.putAll(p1.damageToPlaneswalkers);
        }
        for (var e : p1.combatDamageDealt.entrySet()) {
            Permanent perm = gameQueryService.findPermanentById(gameData, e.getKey());
            if (perm != null) state.combatDamageDealt.put(perm, e.getValue());
        }
        for (var e : p1.combatDamageDealtToPlayer.entrySet()) {
            Permanent perm = gameQueryService.findPermanentById(gameData, e.getKey());
            if (perm != null) state.combatDamageDealtToPlayer.put(perm, e.getValue());
        }
        for (var e : p1.combatDamageDealtToCreatures.entrySet()) {
            Permanent perm = gameQueryService.findPermanentById(gameData, e.getKey());
            if (perm != null) state.combatDamageDealtToCreatures.put(perm, new ArrayList<>(e.getValue()));
        }
        state.combatDamageToBlockingCreatureSources.addAll(p1.combatDamageToBlockingCreatureSources);
        for (var e : p1.combatDamageDealerControllers.entrySet()) {
            Permanent perm = gameQueryService.findPermanentById(gameData, e.getKey());
            if (perm != null) state.combatDamageDealerControllers.put(perm, e.getValue());
        }
        for (var e : p1.combatDamageAmountsToCreatures.entrySet()) {
            Permanent perm = gameQueryService.findPermanentById(gameData, e.getKey());
            if (perm != null) state.combatDamageAmountsToCreatures.put(perm, new HashMap<>(e.getValue()));
        }
        state.deathtouchDamagedAttackerIndices.addAll(p1.deathtouchDamagedAttackerIndices);
        state.deathtouchDamagedDefenderIndices.addAll(p1.deathtouchDamagedDefenderIndices);
    }

    private CombatDamagePhase1State savePhase1State(CombatDamageState state,
                                                     Map<Integer, List<Integer>> blockerMap,
                                                     boolean anyFirstStrike) {
        Map<UUID, Integer> dealtByUUID = new HashMap<>();
        for (var e : state.combatDamageDealt.entrySet()) {
            dealtByUUID.put(e.getKey().getId(), e.getValue());
        }
        Map<UUID, Integer> dealtToPlayerByUUID = new HashMap<>();
        for (var e : state.combatDamageDealtToPlayer.entrySet()) {
            dealtToPlayerByUUID.put(e.getKey().getId(), e.getValue());
        }
        Map<UUID, List<UUID>> dealtToCreaturesByUUID = new HashMap<>();
        for (var e : state.combatDamageDealtToCreatures.entrySet()) {
            dealtToCreaturesByUUID.put(e.getKey().getId(), new ArrayList<>(e.getValue()));
        }
        Map<UUID, UUID> dealerControllersByUUID = new HashMap<>();
        for (var e : state.combatDamageDealerControllers.entrySet()) {
            dealerControllersByUUID.put(e.getKey().getId(), e.getValue());
        }
        Map<UUID, Map<UUID, Integer>> damageAmountsByUUID = new HashMap<>();
        for (var e : state.combatDamageAmountsToCreatures.entrySet()) {
            damageAmountsByUUID.put(e.getKey().getId(), new HashMap<>(e.getValue()));
        }
        return new CombatDamagePhase1State(
                new TreeSet<>(state.deadAttackerIndices), new TreeSet<>(state.deadDefenderIndices),
                new HashMap<>(state.atkDamageTaken), new HashMap<>(state.defDamageTaken),
                dealtByUUID, dealtToPlayerByUUID, dealtToCreaturesByUUID,
                new HashSet<>(state.combatDamageToBlockingCreatureSources), dealerControllersByUUID,
                damageAmountsByUUID,
                state.damageToDefendingPlayer, state.damageRedirectedToGuard,
                state.deathtouchDamageRedirectedToGuard,
                new HashMap<>(state.damageToPlaneswalkers),
                new LinkedHashMap<>(blockerMap), anyFirstStrike,
                new HashSet<>(state.deathtouchDamagedAttackerIndices), new HashSet<>(state.deathtouchDamagedDefenderIndices),
                state.unpreventableDamageToDefendingPlayer,
                new HashMap<>(state.unpreventableAtkDamageTaken), new HashMap<>(state.unpreventableDefDamageTaken));
    }

    private void sendNextCombatDamageAssignment(GameData gameData, List<Permanent> atkBf,
                                                 List<Permanent> defBf, UUID activeId, UUID defenderId) {
        // Attacker assignments first (CR 510.1c before 510.1d); blocker divisions after.
        if (gameData.combatDamagePendingIndices.isEmpty()) {
            sendNextBlockerDamageAssignment(gameData, atkBf, defBf, activeId, defenderId);
            return;
        }
        int atkIdx = gameData.combatDamagePendingIndices.get(0);
        Permanent atk = atkBf.get(atkIdx);
        CombatDamagePhase1State p1 = gameData.combatDamagePhase1State;
        List<Integer> blkIndices = p1.blockerMap.get(atkIdx);
        List<Integer> livingBlockers = new ArrayList<>();
        for (int i : blkIndices) {
            if (!p1.deadDefenderIndices.contains(i)) livingBlockers.add(i);
        }

        List<CombatDamageTarget> domainTargets = new ArrayList<>();
        Set<UUID> domainTargetIds = new HashSet<>();
        for (int blkIdx : livingBlockers) {
            Permanent blk = defBf.get(blkIdx);
            int lethalDamageThreshold = gameQueryService.getLethalDamageThreshold(gameData, blk);
            int damageTaken = p1.defDamageTaken.getOrDefault(blkIdx, 0);
            domainTargets.add(new CombatDamageTarget(
                    blk.getId(), blk.getCard().getName(), lethalDamageThreshold, damageTaken, false));
            domainTargetIds.add(blk.getId());
        }

        boolean isTrample = gameQueryService.hasKeyword(gameData, atk, Keyword.TRAMPLE);
        boolean isDeathtouch = gameQueryService.hasKeyword(gameData, atk, Keyword.DEATHTOUCH);
        boolean assignsAmongDefendingPlayerAndCreatures =
                assignsCombatDamageAmongDefendingPlayerAndCreatures(gameData, atk);
        // Unblocked attacker that may redirect its whole combat damage to one defending creature
        // (e.g. Cunning Giant): offer every defending creature plus the defending player.
        boolean unblockedRedirect = livingBlockers.isEmpty()
                && !atk.isBlockedWithoutBlockers()
                && canRedirectUnblockedDamageToDefendingCreature(gameData, atk, defBf);
        if (unblockedRedirect) {
            for (Permanent def : defBf) {
                if (gameQueryService.isCreature(gameData, def) && domainTargetIds.add(def.getId())) {
                    domainTargets.add(new CombatDamageTarget(def.getId(), def.getCard().getName(),
                            gameQueryService.getLethalDamageThreshold(gameData, def), def.getMarkedDamage(), false));
                }
            }
        }
        if (assignsAmongDefendingPlayerAndCreatures) {
            for (int defIdx = 0; defIdx < defBf.size(); defIdx++) {
                Permanent def = defBf.get(defIdx);
                if (gameQueryService.isCreature(gameData, def) && domainTargetIds.add(def.getId())) {
                    domainTargets.add(new CombatDamageTarget(
                            def.getId(), def.getCard().getName(),
                            gameQueryService.getLethalDamageThreshold(gameData, def),
                            def.getMarkedDamage() + p1.defDamageTaken.getOrDefault(defIdx, 0), false));
                }
            }
            if (domainTargetIds.add(defenderId)) {
                domainTargets.add(new CombatDamageTarget(
                        defenderId, gameData.playerIdToName.get(defenderId), 0, 0, true));
            }
        }
        UUID overflowTarget = atk.getAttackTarget() != null ? atk.getAttackTarget() : defenderId;
        boolean unblocked = livingBlockers.isEmpty() && !atk.isBlockedWithoutBlockers();
        boolean addOverflow = isTrample || assignsCombatDamageAsThoughUnblocked(gameData, atk)
                || unblockedRedirect
                || (assignsAmongDefendingPlayerAndCreatures
                && (unblocked || Objects.equals(overflowTarget, defenderId)));
        if (addOverflow) {
            if (gameData.playerIds.contains(overflowTarget)) {
                String defenderName = gameData.playerIdToName.get(overflowTarget);
                if (domainTargetIds.add(overflowTarget)) {
                    domainTargets.add(new CombatDamageTarget(
                            overflowTarget, defenderName, 0, 0, true));
                }
            } else {
                Permanent pw = gameQueryService.findPermanentById(gameData, overflowTarget);
                if (pw != null && domainTargetIds.add(overflowTarget)) {
                    domainTargets.add(new CombatDamageTarget(
                            overflowTarget, pw.getCard().getName(), 0, 0, true));
                }
            }
        }

        int totalDamage = gameQueryService.getEffectiveCombatDamage(gameData, atk);

        List<String> blockerDescriptions = new ArrayList<>();
        for (int i : livingBlockers) {
            blockerDescriptions.add(defBf.get(i).getCard().getName()
                    + " " + gameQueryService.getEffectiveToughness(gameData, defBf.get(i)) + " toughness");
        }
        log.info("Game {} - Requesting combat damage assignment for [{}]: {} (damage={}, trample={}, deathtouch={}, blockers={})",
                gameData.id, atkIdx, atk.getCard().getName(), totalDamage, isTrample, isDeathtouch,
                blockerDescriptions);

        // CR 702.22j — route the prompt to the defending player when a banding blocker is present.
        UUID assigningPlayer = attackerDamageAssigner(gameData, atk, livingBlockers, defBf, activeId, defenderId);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.CombatDamageAssignment(
                assigningPlayer, atkIdx, atk.getId(), atk.getCard().getName(), totalDamage,
                domainTargets, isTrample, isDeathtouch,
                unblockedRedirect && !assignsAmongDefendingPlayerAndCreatures));
    }

    /**
     * Prompts the defending player to divide a multi-blocking creature's combat damage among the
     * attackers it blocks (CR 510.1d). Reuses the {@code CombatDamageAssignment} interaction; the
     * index carried by the record/answer is the blocker's defending-battlefield index, and
     * {@link #handleCombatDamageAssigned} routes the answer by the answering player.
     */
    private void sendNextBlockerDamageAssignment(GameData gameData, List<Permanent> atkBf,
                                                  List<Permanent> defBf, UUID activeId, UUID defenderId) {
        int blkIdx = gameData.combatDamagePendingBlockerIndices.get(0);
        Permanent blk = defBf.get(blkIdx);
        CombatDamagePhase1State p1 = gameData.combatDamagePhase1State;

        List<CombatDamageTarget> domainTargets = new ArrayList<>();
        for (var bEntry : p1.blockerMap.entrySet()) {
            int atkIdx = bEntry.getKey();
            if (p1.deadAttackerIndices.contains(atkIdx)) continue;
            if (!bEntry.getValue().contains(blkIdx)) continue;
            Permanent atk = atkBf.get(atkIdx);
            domainTargets.add(new CombatDamageTarget(
                    atk.getId(), atk.getCard().getName(),
                    gameQueryService.getLethalDamageThreshold(gameData, atk),
                    atk.getMarkedDamage() + p1.atkDamageTaken.getOrDefault(atkIdx, 0), false));
        }

        if (assignsCombatDamageAmongDefendingPlayerAndCreatures(gameData, blk)) {
            domainTargets.add(new CombatDamageTarget(defenderId, gameData.playerIdToName.get(defenderId), 0, 0, true));
            for (Permanent permanent : defBf) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    domainTargets.add(new CombatDamageTarget(permanent.getId(), permanent.getCard().getName(),
                            gameQueryService.getLethalDamageThreshold(gameData, permanent), permanent.getMarkedDamage(), false));
                }
            }
        }
        if (gameQueryService.hasKeyword(gameData, blk, Keyword.TANTRUM)) {
            domainTargets.add(new CombatDamageTarget(activeId, gameData.playerIdToName.get(activeId), 0, 0, true));
        }

        int totalDamage = gameQueryService.getEffectiveCombatDamage(gameData, blk);
        boolean isDeathtouch = gameQueryService.hasKeyword(gameData, blk, Keyword.DEATHTOUCH);

        log.info("Game {} - Requesting blocker damage division for [{}]: {} (damage={}, attackers={})",
                gameData.id, blkIdx, blk.getCard().getName(), totalDamage,
                domainTargets.stream().map(CombatDamageTarget::name).toList());

        // CR 702.22k — route the prompt to the active player when the blocker is blocking a
        // creature with banding.
        UUID assigningPlayer = blockerDamageAssigner(gameData, blkIdx, p1, atkBf, activeId, defenderId);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.CombatDamageAssignment(
                assigningPlayer, blkIdx, blk.getId(), blk.getCard().getName(), totalDamage,
                domainTargets, false, isDeathtouch, false));
    }
}
