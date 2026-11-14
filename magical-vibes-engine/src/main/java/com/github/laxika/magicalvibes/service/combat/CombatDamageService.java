package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.model.rules.CombatDamageStepEvent;
import com.github.laxika.magicalvibes.model.rules.DamageDealtEvent;
import com.github.laxika.magicalvibes.model.rules.RulesEvent;

import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.model.event.VisualCueType;
import com.github.laxika.magicalvibes.service.event.VisualCueService;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.SaturatingMath;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CombatDamagePhase1State;
import com.github.laxika.magicalvibes.model.CombatDamageState;
import com.github.laxika.magicalvibes.model.CombatDamageTarget;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
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
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageAsThoughUnblockedEffect;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageToDefendingCreatureWhenUnblockedEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;
import com.github.laxika.magicalvibes.model.effect.CombatDamageAssignmentToDefendingPlayerAndCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CombatDamageResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.DefendingPlayerAssignsCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.FirstestStrikeEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceCombatDamageWithMillEffect;
import com.github.laxika.magicalvibes.model.effect.TakeInitiativeEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.replacement.DamageEvent;
import com.github.laxika.magicalvibes.model.replacement.DamagePortion;
import com.github.laxika.magicalvibes.service.effect.replacement.damage.RedirectedDamageRules;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSameNameAsSourcePredicate;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.combat.attack.CombatAttackService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.*;
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

    private static final PermanentHasSameNameAsSourcePredicate SAME_NAME_AS_SOURCE =
            new PermanentHasSameNameAsSourcePredicate();

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
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.github.laxika.magicalvibes.service.trigger.pipeline.DamageTriggers damageTriggers;
    private final com.github.laxika.magicalvibes.service.effect.GrantedTriggeredAbilitySupport grantedTriggeredAbilitySupport;
    private final LifeSupport lifeSupport;
    private final GraveyardTargetingService graveyardTargetingService;
    private final CombatAttackService combatAttackService;
    private final CombatTriggerService combatTriggerService;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport damageSupport;
    private final PermanentCounterSupport permanentCounterSupport;
    private final com.github.laxika.magicalvibes.service.state.StateBasedActionService stateBasedActionService;
    private final com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport battleDefeatSupport;
    private final VisualCueService visualCueService;
    private final TriggeredAbilityStackService triggeredAbilityStackService;
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.github.laxika.magicalvibes.service.effect.normalfx.ProliferateEffectHandler proliferateHandler;

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
        if (askCombatDamageAssignmentModes(gameData, atkBf, defBf, attackingIndices, blockerMap)) {
            return CombatResult.DONE;
        }

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

    /**
     * Asks the controller of each attacking or blocking creature to which several combat damage assignment effects
     * apply (Narod, the Beige Flower and Doran, the Siege Tower) which one applies to that creature, when they would
     * assign different amounts (Gatherer ruling 2024-11-08 on Narod, the Beige Flower). Answered choices are kept for
     * the damage step; the step starts again once every creature has one.
     */
    private boolean askCombatDamageAssignmentModes(GameData gameData, List<Permanent> attackers,
                                                   List<Permanent> defenders, List<Integer> attackingIndices,
                                                   Map<Integer, List<Integer>> blockerMap) {
        List<Permanent> creatures = new ArrayList<>();
        for (int i : attackingIndices) {
            creatures.add(attackers.get(i));
        }
        for (List<Integer> blockers : blockerMap.values()) {
            for (int i : blockers) {
                if (!creatures.contains(defenders.get(i))) creatures.add(defenders.get(i));
            }
        }
        for (Permanent creature : creatures) {
            if (gameData.combatDamageAssignmentModeChoices.containsKey(creature.getId())
                    || !gameQueryService.hasCompetingCombatDamageAssignmentEffects(gameData, creature)) continue;
            UUID controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    controllerId, null, null,
                    new com.github.laxika.magicalvibes.model.ChoiceContext.CombatDamageAssignmentModeChoice(
                            creature.getId(), controllerId),
                    List.of(com.github.laxika.magicalvibes.model.ChoiceContext.CombatDamageAssignmentModeChoice.MANA_VALUE,
                            com.github.laxika.magicalvibes.model.ChoiceContext.CombatDamageAssignmentModeChoice.TOUGHNESS),
                    "Choose how " + creature.getCard().getName() + " assigns combat damage."));
            return true;
        }
        return false;
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
        if (state.pendingOptionalDamageChoices == 0) return parkForProliferateLinks(gameData, state, phase);
        state.awaitingOptionalDamageChoices = true;
        gameData.pendingCombatDamageState = state;
        gameData.pendingCombatDamageFirstestStrikePhase = phase == DamagePhase.FIRSTEST_STRIKE;
        gameData.pendingCombatDamageFirstStrikePhase = phase == DamagePhase.FIRST_STRIKE;
        playerInputService.processNextMayAbility(gameData);
        return true;
    }

    /**
     * Proliferatelink: the controller of each source with the keyword that dealt combat damage proliferates that
     * many times before the damage is checked for lethality, so the step is parked until those prompts are answered.
     * Returns {@code true} while a proliferate prompt waits.
     */
    private boolean parkForProliferateLinks(GameData gameData, CombatDamageState state, DamagePhase phase) {
        if (state.proliferateLinksChecked || !state.collectedPortions.isEmpty()) return false;
        state.proliferateLinksChecked = true;
        state.combatDamageDealt.forEach((source, damage) -> {
            if (damage <= 0 || !gameQueryService.hasKeyword(gameData, source, Keyword.PROLIFERATELINK)) return;
            UUID controllerId = state.combatDamageDealerControllers.getOrDefault(source,
                    gameQueryService.findPermanentController(gameData, source.getId()));
            if (controllerId != null) state.pendingProliferateLinks.merge(controllerId, damage, Integer::sum);
        });
        gameData.pendingCombatDamageFirstestStrikePhase = phase == DamagePhase.FIRSTEST_STRIKE;
        gameData.pendingCombatDamageFirstStrikePhase = phase == DamagePhase.FIRST_STRIKE;
        boolean waiting = beginNextProliferateLink(gameData, state);
        if (waiting) {
            state.awaitingOptionalDamageChoices = true;
            state.parkedForProliferateLinks = true;
            gameData.pendingCombatDamageState = state;
        }
        return waiting;
    }

    private boolean beginNextProliferateLink(GameData gameData, CombatDamageState state) {
        var iterator = state.pendingProliferateLinks.entrySet().iterator();
        while (iterator.hasNext()) {
            var next = iterator.next();
            iterator.remove();
            proliferateHandler.begin(gameData, next.getKey(), next.getValue());
            if (gameData.interaction.isAwaitingInput()) return true;
        }
        return false;
    }

    /** Whether the parked combat damage step is waiting on proliferatelink proliferating. */
    public boolean isParkedForProliferateLinks(GameData gameData) {
        return gameData.pendingCombatDamageState != null && gameData.pendingCombatDamageState.parkedForProliferateLinks;
    }

    /**
     * Called when a proliferate chain of the parked step ends: starts the next controller's proliferating, or, with
     * none left, unparks the step. Returns {@code true} while another proliferate prompt waits.
     */
    public boolean continueAfterProliferateLink(GameData gameData) {
        CombatDamageState state = gameData.pendingCombatDamageState;
        if (beginNextProliferateLink(gameData, state)) return true;
        state.parkedForProliferateLinks = false;
        return false;
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

        // "Combat damage uses the stack" (Stack of Paperwork): the assigned damage is one object on the stack, which a
        // rule makes as the step begins, not an ability a permanent's trigger event made, so no "triggers an
        // additional time" effect counts it: the door puts it as one object of the rule that made it.
        StackEntry damageEntry = TriggeredAbilityStackService.madeAbility(sourceCard, gameData.activePlayerId,
                "Combat damage", List.of(new CombatDamageResolutionEffect()), 0, sourcePermanentId);
        damageEntry.setNonTargeting(true);
        triggeredAbilityStackService.putMade(gameData, damageEntry);
        gameLogService.append(gameData, GameLog.text("Combat damage is put on the stack."));
        return CombatResult.AUTO_PASS_ONLY;
    }

    /** Resolves the pending combat-damage stack object after both players pass priority. */
    public void resolvePendingCombatDamage(GameData gameData) {
        CombatDamageState state = gameData.pendingCombatDamageState;
        if (state != null && !state.collectedPortions.isEmpty()) {
            DamagePhase phase = gameData.pendingCombatDamageFirstestStrikePhase
                    ? DamagePhase.FIRSTEST_STRIKE
                    : gameData.pendingCombatDamageFirstStrikePhase
                    ? DamagePhase.FIRST_STRIKE
                    : DamagePhase.REGULAR;
            processCollectedCombatDamage(gameData, state);
            if (parkOptionalDamageChoices(gameData, state, phase)) {
                return;
            }
        }
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


    /** Emits {@code event} as an occurrence of its own: the abilities it triggers are matched at once (CR 603.2). */
    private static void emit(GameData gameData, RulesEvent event) {
        gameData.rulesEvents.separately(() -> gameData.rulesEvents.emit(event));
    }

    private static void emitCombatDamageEvent(GameData gameData, CombatDamageStepEvent.Stage stage,
                                              CombatDamageState state, UUID activeId, UUID defenderId) {
        emit(gameData, new CombatDamageStepEvent(stage, state, activeId, defenderId));
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
        state.redirectedCombatDamageToPlayers.forEach((playerId, damageBySource) ->
                damagedPlayerSources.put(playerId, new LinkedHashSet<>(damageBySource.keySet())));
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
        gameData.combatSharedDamagePreventionAllocations.clear();
        gameData.combatDamageAssignmentModeChoices.clear();
        applyPendingDralnuReplacements(gameData, state);
        applyPlaneswalkerDamage(gameData, state);
        // CR 510.2 — lifelink gains life as the simultaneous damage is dealt, so it can't change
        // whether a life-dependent prevention applied to that same damage. Before dead creatures leave.
        processLifelink(gameData, state.combatDamageDealt, lifelinkDealers);
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

        emitCombatDamageEvent(gameData, CombatDamageStepEvent.Stage.DEALT, state, activeId, defenderId);
        Set<StackEntry> heldDamageTriggers = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        heldDamageTriggers.addAll(state.heldGlobalDamageTriggers);
        heldDamageTriggers.addAll(state.heldSourceDamageReflectionTriggers);
        heldDamageTriggers.addAll(state.heldSelfAllyDamageReflectionTriggers);
        heldDamageTriggers.addAll(state.heldWatcherAllyDamageReflectionTriggers);
        boolean damageEventTriggersQueued =
                gameData.stack.size() - heldDamageTriggers.size() > stackSizeBeforeDamageEvents;
        stateBasedActionService.performStateBasedActions(gameData);
        gameData.stack.removeIf(heldDamageTriggers::contains);

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
        gameData.stack.addAll(state.heldGlobalDamageTriggers);

        // Death triggers were already collected by the SBA pass above. They must remain on the
        // stack for normal priority rather than being folded into the engine's auto-resolved
        // combat-damage trigger batch below.
        updateMonarchFromCombatDamage(gameData, state, defenderId);
        queueInitiativeTransferFromCombatDamage(gameData, state, defenderId);
        emitCombatDamageEvent(gameData, CombatDamageStepEvent.Stage.AFTER_STATE_BASED_ACTIONS, state, activeId,
                defenderId);
        recordCombatDamageBySource(gameData, state);

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
                // "Whenever a creature deals combat damage to the monarch, its controller becomes the monarch": no
                // source, controlled by the monarch (CR 726.2); "its controller" is the entry's non-targeting player.
                UUID newMonarchId = controllerId;
                triggeredAbilityStackService.putInherent(gameData, gameData.monarchPlayerId,
                        "Combat damage causes a player to become the monarch",
                        List.of(new com.github.laxika.magicalvibes.model.effect.TargetPlayerBecomesMonarchEffect()),
                        entry -> {
                            entry.setTargetId(newMonarchId);
                            entry.setNonTargeting(true);
                        });
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
        // "Whenever one or more creatures a player controls deal combat damage to the player who has the initiative, the
        // controller of those creatures takes the initiative": no source, controlled by the player who has the
        // initiative (CR 727.2); "the controller of those creatures" is the entry's non-targeting player.
        for (UUID controllerId : controllers) {
            triggeredAbilityStackService.putInherent(gameData, gameData.initiativePlayerId,
                    "Combat damage causes a player to take the initiative", List.of(new TakeInitiativeEffect(true)),
                    entry -> {
                        entry.setTargetId(controllerId);
                        entry.setNonTargeting(true);
                    });
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
        state.reflectDamageSourceIds = List.copyOf(gameData.reflectDamageToSourceControllerShields);
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
        if (phase == DamagePhase.FIRSTEST_STRIKE) {
            // CR 510.4 (by analogy): which creatures belong to the firstest-strike step is fixed as the
            // step begins; a creature that gains firstest strike later deals damage in the regular step.
            snap.attackerStats().forEach((idx, stats) -> {
                if (stats.firstestStrike()) gameData.creaturesInFirstestStrikeDamageStepThisCombat.add(atkBf.get(idx).getId());
            });
            snap.defenderStats().forEach((idx, stats) -> {
                if (stats.firstestStrike()) gameData.creaturesInFirstestStrikeDamageStepThisCombat.add(defBf.get(idx).getId());
            });
        }

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
                        && protectionPreventsCombatDamage(gameData, target, blocker))) continue;
                if (!applyCombatCreatureDamage(gameData, state, blocker, stats, target, damage)) {
                    state.combatDamageDealt.merge(blocker, damage, Integer::sum);
                    recordCombatDamageToCreature(gameData, state, blocker, defenderId, target, damage);
                }
            }
        }
        // When combat damage uses the stack, the assignment is final but the damage is dealt (and its
        // replacement and prevention effects apply) only when the stack object resolves.
        if (!shouldPutCombatDamageOnStack(gameData)) {
            processCollectedCombatDamage(gameData, state);
        }
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
                                  boolean tookPartInFirstestStrikeStep,
                                  boolean firstStrike,
                                  boolean doubleStrike, boolean deathtouch, boolean trample,
                                  boolean infect, boolean preventedFromDealingCombatDamage,
                                  CardColor color) {

        boolean participatesInDamagePhase(DamagePhase phase) {
            return switch (phase) {
                case FIRSTEST_STRIKE -> firstestStrike;
                case FIRST_STRIKE -> firstStrike || doubleStrike;
                case REGULAR -> doubleStrike
                        || (!firstStrike && (!firstestStrike || !tookPartInFirstestStrikeStep));
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
                    if (protectionPreventsCombatDamage(gameData, atk, blk)) {
                        attackerProtectedFromBlocker.add(DamagePhaseSnapshot.pairKey(atkIdx, blkIdx));
                    }
                    if (protectionPreventsCombatDamage(gameData, blk, atk)) {
                        blockerProtectedFromAttacker.add(DamagePhaseSnapshot.pairKey(atkIdx, blkIdx));
                    }
                }
            }
            return new DamagePhaseSnapshot(attackerStats, defenderStats,
                    gameQueryService.isDamagePreventable(gameData, true),
                    attackerProtectedFromBlocker, blockerProtectedFromAttacker);
        });
    }

    /**
     * Whether protection keeps combat damage from {@code source} from reaching {@code target} at all. A creature
     * whose own damage prevention removes +1/+1 counters (Phantom Centaur) is not shielded up front: its controller
     * chooses between that prevention and protection (CR 616.1), so the damage goes on to the prevention step.
     */
    private boolean protectionPreventsCombatDamage(GameData gameData, Permanent target, Permanent source) {
        return gameQueryService.hasProtectionFromDamageSource(gameData, target, source)
                && gameQueryService.getActiveStaticEffects(gameData, target).stream().noneMatch(
                com.github.laxika.magicalvibes.model.effect.PreventDamageAndRemovePlusOnePlusOneCountersEffect.class::isInstance);
    }

    private CombatantStats snapshotCombatant(GameData gameData, Permanent creature) {
        return new CombatantStats(
                gameQueryService.getEffectiveCombatDamage(gameData, creature),
                gameQueryService.getLethalDamageThreshold(gameData, creature),
                gameQueryService.hasActiveStaticEffect(gameData, creature, FirstestStrikeEffect.class),
                gameData.creaturesInFirstestStrikeDamageStepThisCombat.contains(creature.getId()),
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
                    if (!(damagePreventable && protectionPreventsCombatDamage(gameData, blk, atk))) {
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

    /**
     * Gains life for each creature of {@code lifelinkDealers} once, for all the combat damage it dealt in this
     * damage step ({@code combatDamageDealt} holds each creature's sum), so its controller's "whenever you gain
     * life" abilities see one life gain per lifelink creature.
     */
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
                emit(gameData, new DamageDealtEvent.CombatToPermanent(perm, dmg));
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
     * Removes dead permanent IDs from surviving blockers' blockingTargetIds. A blocker whose blocked
     * attackers all died stays a blocking creature until it is removed from combat (CR 506.4, CR 509.1h).
     */
    private void clearOrphanedBlockingState(List<Permanent> battlefield, Set<UUID> deadIds) {
        if (deadIds.isEmpty()) return;
        for (Permanent p : battlefield) {
            if (!p.isBlocking()) continue;
            p.getBlockingTargetIds().removeAll(deadIds);
            if (p.getBlockingTargetIds().isEmpty()) {
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
            totalNormal = SaturatingMath.add(totalNormal, recipientState.damageToDefendingPlayer);
            totalInfect = SaturatingMath.add(totalInfect, recipientState.poisonDamageToDefendingPlayer);
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
                int lifeAfterDamage = SaturatingMath.subtract(currentLife, state.damageToDefendingPlayer);
                // Worship / Elderscale Wurm: combat damage can't reduce the player's life total past an
                // active floor. The full damage is still dealt; only the life reduction is capped.
                // 0 means no active floor — do not clamp (life may go negative).
                int lifeFloor = gameQueryService.damageLifeFloor(gameData, defenderId, currentLife);
                if (lifeFloor > 0 && lifeAfterDamage < lifeFloor) {
                    lifeAfterDamage = lifeFloor;
                }
                int lifeLoss = SaturatingMath.multiply(SaturatingMath.subtract(currentLife, lifeAfterDamage),
                        gameQueryService.opponentLifeLossMultiplier(gameData, defenderId));
                int newLife = SaturatingMath.subtract(currentLife, lifeLoss);
                gameData.playerLifeTotals.put(defenderId, newLife);
                int lifeLost = currentLife - newLife;
                String logEntry = gameData.playerIdToName.get(defenderId) + " takes " + state.damageToDefendingPlayer + " combat damage.";
                gameLogService.append(gameData, GameLog.text(logEntry));
                if (lifeLost > 0) {
                    lifeSupport.lifeLost(gameData, defenderId, lifeLost);
                }
            } else {
                String playerName = gameData.playerIdToName.get(defenderId);
                String message = gameQueryService.damageDoesNotCauseLifeLoss(gameData, defenderId)
                        ? playerName + " takes " + state.damageToDefendingPlayer + " combat damage."
                        : playerName + "'s life total can't change.";
                gameLogService.append(gameData, GameLog.text(message));
            }
        }
        damageSupport.finishLifeLossDeparture(gameData, defenderId);

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
            Map<UUID, Integer> damageBySource = new LinkedHashMap<>();
            for (var sourceDamage : state.combatDamageDealtToPlayer.entrySet()) {
                if (sourceDamage.getValue() > 0) {
                    damageBySource.put(sourceDamage.getKey().getId(), sourceDamage.getValue());
                }
            }
            emit(gameData, new DamageDealtEvent.CombatToPlayer(defenderId, damageDealt, damageBySource));
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
                emit(gameData, new DamageDealtEvent.CombatToPermanent(pw, damage));
                gameLogService.append(gameData, GameLog.cardThen(pw.getCard(), " takes " + damage + " combat damage ("
                        + pw.getCounterCount(CounterType.DEFENSE) + " defense remaining)."));
                battleDefeatSupport.checkAfterDefenseRemoved(gameData, pw);
                continue;
            }
            gameData.recordDamageToPermanent(pw.getId(), damage);
            emit(gameData, new DamageDealtEvent.CombatToPermanent(pw, damage));
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
                emitCombatDamageCue(gameData, combatSource(gameData, portion), null, event.recipientPlayerId(),
                        portion.amount());
                dealRedirectedCombatDamageToPlayer(gameData, state, event.recipientPlayerId(), portion,
                        damageSource, sourceControllerId);
            } else {
                Permanent targetPerm = gameQueryService.findPermanentById(gameData, event.recipientPermanentId());
                if (targetPerm != null) {
                    emitCombatDamageCue(gameData, combatSource(gameData, portion), targetPerm.getId(), null,
                            portion.amount());
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
            int lifeLoss = SaturatingMath.multiply(damage, gameQueryService.opponentLifeLossMultiplier(gameData, targetId));
            gameData.playerLifeTotals.put(targetId, gameQueryService.lifeAfterDamage(gameData, targetId, lifeLoss));
        }
        gameData.recordCombatDamageToPlayer(targetId, damage);
        if (damageSource != null) {
            state.combatDamageDealt.merge(damageSource, damage, Integer::sum);
            state.redirectedCombatDamageToPlayers
                    .computeIfAbsent(targetId, ignored -> new LinkedHashMap<>()).merge(damageSource, damage, Integer::sum);
            if (sourceControllerId != null) {
                state.combatDamageDealerControllers.putIfAbsent(damageSource, sourceControllerId);
            }
        }
        boolean artifactSource = damageSource != null && gameQueryService.isArtifact(gameData, damageSource);
        gameData.recordDamageToPlayer(targetId, damage, artifactSource ? damage : 0);
        gameData.recordDamageDealtBySourceToPlayer(sourceId, targetId, damage);
        gameData.recordDamageRecipientBySource(sourceId, targetId);
        if (damageSource != null) {
            gameData.recordCombatDamageByCreatureNameToPlayer(damageSource.getCard().getName(), targetId);
        }
        emit(gameData, new DamageDealtEvent.CombatToPlayer(targetId, damage, Map.of(sourceId, damage)));
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
        emit(gameData, new DamageDealtEvent.CombatToPermanent(targetPerm, effectiveDamage));
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
            if (pw.getCard().hasType(CardType.PLANESWALKER)
                    && gameQueryService.hasKeyword(gameData, atk, Keyword.TRAMPLE_OVER_PLANESWALKERS)) {
                // CR 702.19c: once damage at least equal to the planeswalker's loyalty (counting damage
                // other creatures assign to it in this step) is assigned to it, the excess is assigned
                // to the planeswalker's controller.
                int alreadyAssigned = state.collectedPortions.getOrDefault(pw.getId(), List.of()).stream()
                        .mapToInt(DamagePortion::amount).sum();
                int toPlaneswalker = Math.min(damage,
                        Math.max(0, pw.getCounterCount(CounterType.LOYALTY) - alreadyAssigned));
                UUID planeswalkerControllerId = gameQueryService.findPermanentController(gameData, pw.getId());
                collectCombatDamage(gameData, state, atk, atkStats, pw.getId(), pw, toPlaneswalker, sourceUnblocked);
                if (damage > toPlaneswalker && planeswalkerControllerId != null) {
                    collectCombatDamage(gameData, state, atk, atkStats, planeswalkerControllerId, null,
                            damage - toPlaneswalker, sourceUnblocked);
                }
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
        StackEntry damageEntry = StackEntry.carrier(effect.damageEntryType(), ability.sourceCard(),
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
            if (state.reflectDamageSourceIds.contains(portion.sourceId())
                    && !gameData.reflectDamageToSourceControllerShields.contains(portion.sourceId())) {
                gameData.reflectDamageToSourceControllerShields.add(portion.sourceId());
            }
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

    /** One cue per damage assignment, once replacement and prevention effects have applied to it. */
    private void emitCombatDamageCue(GameData gameData, Permanent source, UUID targetPermanentId,
                                     UUID targetPlayerId, int amount) {
        visualCueService.emit(gameData,
                targetPlayerId != null ? VisualCueType.DAMAGE_TO_PLAYER : VisualCueType.DAMAGE_TO_PERMANENT,
                VisualCueService.Source.of(source), targetPermanentId, targetPlayerId, amount);
    }

    private void recordCombatEventToPlayer(GameData gameData, CombatDamageState state, DamageEvent event) {
        UUID playerId = event.recipientPlayerId();
        for (DamagePortion portion : event.portions()) {
            Permanent source = combatSource(gameData, portion);
            int amount = portion.amount();
            state.combatDamageDealt.merge(source, amount, Integer::sum);
            state.combatDamageDealtToPlayer.merge(source, amount, SaturatingMath::add);
            state.combatDamagePlayerRecipients.put(source, playerId);
            if (amount <= 0) continue;
            emitCombatDamageCue(gameData, source, null, playerId, amount);
            if (portion.counterDamage()) {
                state.poisonDamageToDefendingPlayer = SaturatingMath.add(state.poisonDamageToDefendingPlayer, amount);
                state.infectDamageToPlayers.merge(playerId, amount, SaturatingMath::add);
            } else {
                state.damageToDefendingPlayer = SaturatingMath.add(state.damageToDefendingPlayer, amount);
                state.normalDamageToPlayers.merge(playerId, amount, SaturatingMath::add);
                if (portion.unpreventable()) {
                    state.unpreventableDamageToDefendingPlayer = SaturatingMath.add(state.unpreventableDamageToDefendingPlayer, amount);
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
            emitCombatDamageCue(gameData, source, target.getId(), null, amount);
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
            if (portion.counterDamage() || index >= 0) {
                emitCombatDamageCue(gameData, source, target.getId(), null, amount);
            }
            if (portion.counterDamage()) {
                recordCounterCombatDamage(gameData, source, sourceControllerId, portion, target);
            } else if (index >= 0) {
                damageTaken.merge(index, amount, SaturatingMath::add);
                takenBySource.computeIfAbsent(index, ignored -> new HashMap<>())
                        .merge(source.getId(), amount, SaturatingMath::add);
                if (portion.unpreventable()) {
                    unpreventableTaken.merge(index, amount, SaturatingMath::add);
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
        emit(gameData, new DamageDealtEvent.CombatToPermanent(target, amount));
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
        emit(gameData, new DamageDealtEvent.CombatToCreature(source, target, damage));
    }

    private boolean hasFirstOrDoubleStrike(GameData gameData, Permanent creature) {
        return gameQueryService.hasKeyword(gameData, creature, Keyword.FIRST_STRIKE)
                || gameQueryService.hasKeyword(gameData, creature, Keyword.DOUBLE_STRIKE);
    }

    /** Whether the creature had firstest strike as this combat's firstest-strike damage step began. */
    private boolean tookPartInFirstestStrikeStep(GameData gameData, Permanent creature) {
        return hasFirstestStrike(gameData, creature)
                && gameData.creaturesInFirstestStrikeDamageStepThisCombat.contains(creature.getId());
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
            for (CardEffect effect : perm.getCard().getStaticEffects()) {
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
        List<CardEffect> effects = new ArrayList<>(attacker.getCard().getStaticEffects());
        effects.addAll(attacker.getTemporaryStaticEffects());
        effects.addAll(gameQueryService.getGrantedEffects(gameData, attacker));
        UUID attackerControllerId = gameData.findControllerOf(attacker);
        if (attackerControllerId != null) {
            effects.addAll(gameData.playerStaticEffectsUntilEndOfTurn.getOrDefault(attackerControllerId, List.of()));
        }
        effects.addAll(attachedAssignAsThoughUnblockedEffects(gameData, attacker, attackerControllerId));
        return effects;
    }

    /**
     * The {@link AssignCombatDamageAsThoughUnblockedEffect}s that Auras attached to {@code attacker}
     * carry as their own static abilities (Indomitable Might). They are not abilities of the
     * creature (CR 113.10), so they keep applying after the creature loses all its abilities, but
     * not once the Aura itself has lost its abilities or its effects are ignored for the creature's
     * controller (Volrath's Curse).
     */
    private List<CardEffect> attachedAssignAsThoughUnblockedEffects(GameData gameData, Permanent attacker,
                                                                   UUID attackerControllerId) {
        List<CardEffect> effects = new ArrayList<>();
        gameData.forEachPermanent((controllerId, attachment) -> {
            if (!attachment.isAttached() || !attacker.getId().equals(attachment.getAttachedTo())
                    || attachment.isAuraEffectsIgnoredFor(attackerControllerId)
                    || gameQueryService.hasLostPrintedAbilities(gameData, attachment)) {
                return;
            }
            attachment.getCard().getStaticEffects().stream()
                    .filter(AssignCombatDamageAsThoughUnblockedEffect.class::isInstance)
                    .forEach(effects::add);
        });
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
                            : (!atkFirstStrike && !tookPartInFirstestStrikeStep(gameData, bAtk) || atkDoubleStrike);
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
                        : (!blkFirstStrike && !tookPartInFirstestStrikeStep(gameData, blk) || blkDoubleStrike);
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
