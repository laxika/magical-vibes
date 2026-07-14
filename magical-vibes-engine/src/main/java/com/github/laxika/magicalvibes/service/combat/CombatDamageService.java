package com.github.laxika.magicalvibes.service.combat;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageLoot;
import com.github.laxika.magicalvibes.model.action.DelayedCombatDamageReflection;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.CombatDamagePhase1State;
import com.github.laxika.magicalvibes.model.DamageRedirectShield;
import com.github.laxika.magicalvibes.model.SourceDamageRedirectShield;
import com.github.laxika.magicalvibes.model.CombatDamageTarget;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageAsThoughUnblockedEffect;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageToDefendingCreatureWhenUnblockedEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawAndDiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGetsPoisonCounterEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetOpponentOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsRepeatOnDuplicateEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToControlledCreatureCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToDamageDealtEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceCombatDamageWithMillEffect;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerCantGainLifeRestOfGameEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentsOnCombatDamageToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyPermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfAndAttachToCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.SphinxAmbassadorEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardOrControllerDrawsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.github.laxika.magicalvibes.model.CounterType;

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

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;
    private final DamagePreventionService damagePreventionService;
    private final GraveyardService graveyardService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final TriggerCollectionService triggerCollectionService;
    private final LifeSupport lifeSupport;
    private final CombatAttackService combatAttackService;
    private final CombatTriggerService combatTriggerService;

    /**
     * Resolves combat damage for the current combat phase.
     */
    public CombatResult resolveCombatDamage(GameData gameData) {
        if (gameData.preventAllCombatDamage) {
            String logEntry = "All combat damage is prevented.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return CombatResult.ADVANCE_AND_AUTO_PASS;
        }

        UUID activeId = gameData.activePlayerId;
        UUID defenderId = gameQueryService.getOpponentId(gameData, activeId);

        List<Permanent> atkBf = gameData.playerBattlefields.get(activeId);
        List<Permanent> defBf = gameData.playerBattlefields.get(defenderId);

        // Re-entry: if pending damage assignments remain, send next assignment request
        if (gameData.combatDamagePhase1Complete && !gameData.combatDamagePendingIndices.isEmpty()) {
            sendNextCombatDamageAssignment(gameData, atkBf, defBf, activeId, defenderId);
            return CombatResult.DONE;
        }

        // Check for combat damage redirect (e.g. Kjeldoran Royal Guard)
        Permanent redirectTarget = gameData.combatDamageRedirectTarget != null
                ? gameQueryService.findPermanentById(gameData, gameData.combatDamageRedirectTarget) : null;

        List<Integer> attackingIndices = combatAttackService.getAttackingCreatureIndices(gameData, activeId);

        Map<Integer, List<Integer>> blockerMap = buildBlockerMap(atkBf, defBf, attackingIndices);

        // Check if any combat creature has first strike or double strike
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

        // First-strike combat damage step. Its triggers resolve before regular combat damage.
        if (!gameData.combatDamageFirstStrikeStepComplete
                && !gameData.combatDamagePhase1Complete
                && anyFirstStrike) {
            resolveDamagePhase(gameData, state, blockerMap, atkBf, defBf,
                    attackingIndices, activeId, defenderId, redirectTarget, true);
            CombatResult result = finishCombatDamageStep(gameData, state, atkBf, defBf,
                    activeId, defenderId, redirectTarget);
            gameData.combatDamageFirstStrikeStepComplete = true;
            return result == CombatResult.ADVANCE_AND_AUTO_PASS ? CombatResult.AUTO_PASS_ONLY : result;
        }

        // Check if any phase 2 attackers need manual damage assignment
        if (!gameData.combatDamagePhase1Complete) {
            List<Integer> needsManual = gameQueryService.withQueryScope(gameData, () -> {
                List<Integer> result = new ArrayList<>();
                for (var bEntry : blockerMap.entrySet()) {
                    int bAtkIdx = bEntry.getKey();
                    if (state.deadAttackerIndices.contains(bAtkIdx)) continue;
                    Permanent bAtk = atkBf.get(bAtkIdx);
                    boolean bAtkSkipPhase2 = gameQueryService.hasKeyword(gameData, bAtk, Keyword.FIRST_STRIKE)
                            && !gameQueryService.hasKeyword(gameData, bAtk, Keyword.DOUBLE_STRIKE);
                    if (bAtkSkipPhase2) continue;
                    if (gameQueryService.isPreventedFromDealingDamage(gameData, bAtk, true)) continue;
                    List<Integer> livingBlockers = new ArrayList<>();
                    for (int i : bEntry.getValue()) {
                        if (!state.deadDefenderIndices.contains(i)) livingBlockers.add(i);
                    }
                    if (needsManualDamageAssignment(gameData, bAtk, livingBlockers, defenderId, defBf)) {
                        result.add(bAtkIdx);
                    }
                }
                return result;
            });
            if (!needsManual.isEmpty()) {
                gameData.combatDamagePhase1State = savePhase1State(state, blockerMap, anyFirstStrike);
                gameData.combatDamagePhase1Complete = true;
                gameData.combatDamagePendingIndices.addAll(needsManual);
                sendNextCombatDamageAssignment(gameData, atkBf, defBf, activeId, defenderId);
                return CombatResult.DONE;
            }
        }

        resolveDamagePhase(gameData, state, blockerMap, atkBf, defBf,
                attackingIndices, activeId, defenderId, redirectTarget, false);

        gameData.combatDamageFirstStrikeStepComplete = false;
        return finishCombatDamageStep(gameData, state, atkBf, defBf, activeId, defenderId, redirectTarget);
    }

    private CombatResult finishCombatDamageStep(GameData gameData, CombatDamageState state,
                                                 List<Permanent> atkBf, List<Permanent> defBf,
                                                 UUID activeId, UUID defenderId,
                                                 Permanent redirectTarget) {
        resolveRedirectedDamage(gameData, state, redirectTarget);

        // CR 510.1 — Snapshot Phyrexian Unlife infect conversion before lifelink changes life totals.
        // All combat damage is simultaneous, so the infect check must use pre-damage life.
        state.defenderDamageAsInfect = gameQueryService.shouldDamageBeDealtAsInfect(gameData, defenderId);

        // Process lifelink before removing dead creatures
        processLifelink(gameData, state.combatDamageDealt);
        processGainLifeEqualToDamageDealt(gameData, state.combatDamageDealt);
        processGainLifeEqualToControlledCreatureCombatDamage(gameData, state.combatDamageDealt);

        // Collect ON_DEALT_DAMAGE trigger data before dead creatures are removed from battlefield
        List<DealtDamageTriggerData> dealtDamageTriggerData = collectDealtDamageTriggerData(gameData, state);

        // Update markedDamage on creatures that took combat damage (CR 704.5g)
        updateMarkedDamageFromCombat(gameData, atkBf, defBf, state);

        List<String> deadCreatureNames = removeDeadCreatures(gameData, state, atkBf, defBf, activeId, defenderId);

        applyPlayerDamage(gameData, state, defenderId);
        applyPlaneswalkerDamage(gameData, state);

        if (!deadCreatureNames.isEmpty()) {
            String logEntry = String.join(", ", deadCreatureNames) + " died in combat.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }

        log.info("Game {} - Combat damage resolved: {} damage to defender, {} creatures died",
                gameData.id, state.damageToDefendingPlayer, state.deadAttackerIndices.size() + state.deadDefenderIndices.size());

        // Check win condition
        if (gameOutcomeService.checkWinCondition(gameData)) {
            return CombatResult.DONE;
        }

        int stackSizeBeforeDamageTriggers = gameData.stack.size();
        processCombatDamageToCreatureTriggers(gameData, state.combatDamageDealtToCreatures, state.combatDamageDealerControllers);

        // Process ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE reflection triggers (e.g. Greatbow Doyen)
        processAllyDealtDamageToCreatureReflectionTriggers(gameData, state);

        // Process ON_DEALT_DAMAGE triggers (e.g. Nested Ghoul)
        processDealtDamageTriggers(gameData, dealtDamageTriggerData);

        // Process ON_ANY_CREATURE_DEALT_DAMAGE triggers (e.g. Death Pits of Rath)
        processAnyCreatureDealtDamageTriggers(gameData, state);

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

        // Process combat damage to player triggers (e.g. Cephalid Constable)
        processCombatDamageToPlayerTriggers(gameData, state.combatDamageDealtToPlayer, activeId, defenderId);

        // Process delayed combat damage loot triggers (e.g. Jace, Cunning Castaway's +1)
        processDelayedCombatDamageLootTriggers(gameData, state.combatDamageDealtToPlayer, activeId);

        // Process combat damage reflection triggers (e.g. Harsh Justice)
        processCombatDamageReflectionTriggers(gameData, state.combatDamageDealtToPlayer, activeId, defenderId);

        // Process defender-side damage triggers (e.g. Dissipation Field)
        for (var dmgEntry : state.combatDamageDealtToPlayer.entrySet()) {
            if (dmgEntry.getValue() > 0) {
                triggerCollectionService.checkDamageDealtToControllerTriggers(gameData, defenderId, dmgEntry.getKey().getId(), true);
            }
        }

        combatTriggerService.reorderTriggersAPNAP(gameData, stackSizeBeforeDamageTriggers, activeId);
        if (gameData.interaction.isAwaitingInput()) {
            return CombatResult.DONE;
        }

        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return CombatResult.DONE;
        }

        if (gameData.stack.size() > stackSizeBeforeDamageTriggers) {
            return CombatResult.AUTO_PASS_RESOLVE_COMBAT_TRIGGERS;
        }

        return CombatResult.ADVANCE_AND_AUTO_PASS;
    }

    /**
     * Processes a player's combat damage assignment for a single attacker.
     */
    public void handleCombatDamageAssigned(GameData gameData, Player player, int attackerIndex, Map<UUID, Integer> assignments) {
        if (!gameData.combatDamagePhase1Complete) {
            throw new IllegalStateException("Not in combat damage assignment phase");
        }
        if (!gameData.combatDamagePendingIndices.contains(attackerIndex)) {
            throw new IllegalStateException("Attacker index " + attackerIndex + " is not pending assignment");
        }

        UUID activeId = gameData.activePlayerId;
        if (!player.getId().equals(activeId)) {
            throw new IllegalStateException("Only the active player can assign combat damage");
        }
        UUID defenderId = gameQueryService.getOpponentId(gameData, activeId);
        List<Permanent> atkBf = gameData.playerBattlefields.get(activeId);
        List<Permanent> defBf = gameData.playerBattlefields.get(defenderId);
        Permanent atk = atkBf.get(attackerIndex);
        int totalDamage = gameQueryService.getEffectiveCombatDamage(gameData, atk);

        // Validate total damage
        int totalAssigned = 0;
        for (int assigned : assignments.values()) {
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
                && canRedirectUnblockedDamageToDefendingCreature(gameData, atk, defenderId, defBf);

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
        boolean canTargetOverflow = gameQueryService.hasKeyword(gameData, atk, Keyword.TRAMPLE)
                || assignsCombatDamageAsThoughUnblocked(atk)
                || unblockedRedirect;
        if (canTargetOverflow) {
            validTargetIds.add(overflowTargetId);
        }

        for (UUID targetId : assignments.keySet()) {
            if (!validTargetIds.contains(targetId)) {
                throw new IllegalStateException("Invalid damage target: " + targetId);
            }
        }

        if (unblockedRedirect) {
            int recipients = 0;
            for (int assigned : assignments.values()) {
                if (assigned > 0) recipients++;
            }
            if (recipients > 1) {
                throw new IllegalStateException(
                        "Unblocked combat damage must be assigned to a single recipient");
            }
        }

        // Validate trample: each blocker must receive at least lethal damage
        if (gameQueryService.hasKeyword(gameData, atk, Keyword.TRAMPLE)) {
            boolean atkHasDeathtouchForValidation = gameQueryService.hasKeyword(gameData, atk, Keyword.DEATHTOUCH);

            // Compute total lethal needed across all blockers
            int totalLethalNeeded = 0;
            for (int blkIdx : livingBlockers) {
                Permanent blk = defBf.get(blkIdx);
                int alreadyTaken = blk.getMarkedDamage() + p1.defDamageTaken.getOrDefault(blkIdx, 0);
                int lethal = atkHasDeathtouchForValidation
                        ? Math.max(0, 1 - alreadyTaken)
                        : Math.max(0, gameQueryService.getEffectiveToughness(gameData, blk) - alreadyTaken);
                totalLethalNeeded += lethal;
            }

            if (totalDamage >= totalLethalNeeded) {
                // Enough damage to deal lethal to all blockers: enforce per-blocker minimums
                for (int blkIdx : livingBlockers) {
                    Permanent blk = defBf.get(blkIdx);
                    int alreadyTaken = blk.getMarkedDamage() + p1.defDamageTaken.getOrDefault(blkIdx, 0);
                    int lethal = atkHasDeathtouchForValidation
                            ? Math.max(0, 1 - alreadyTaken)
                            : Math.max(0, gameQueryService.getEffectiveToughness(gameData, blk) - alreadyTaken);
                    int assigned = assignments.getOrDefault(blk.getId(), 0);
                    if (assigned < lethal) {
                        throw new IllegalStateException("Trample: must assign at least " + lethal
                                + " damage to " + blk.getCard().getName());
                    }
                }
            } else {
                // Not enough damage to deal lethal to all blockers:
                // all damage must go to blockers, none can trample over
                if (assignments.containsKey(overflowTargetId)) {
                    throw new IllegalStateException(
                            "Trample: not enough damage to deal lethal to all blockers, "
                                    + "all damage must be assigned to blockers");
                }
            }
        }

        // Validate non-trample non-unblocked: no damage to overflow target
        if (!canTargetOverflow && assignments.containsKey(overflowTargetId)) {
            throw new IllegalStateException("Cannot assign damage to " +
                    (gameData.playerIds.contains(overflowTargetId) ? "defending player" : "planeswalker"));
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


    private void resolveDamagePhase(GameData gameData, CombatDamageState state,
                                     Map<Integer, List<Integer>> blockerMap,
                                     List<Permanent> atkBf, List<Permanent> defBf,
                                     List<Integer> attackingIndices,
                                     UUID activeId, UUID defenderId,
                                     Permanent redirectTarget, boolean isFirstStrikePhase) {
        // CR 510.4: all combat damage in this step is dealt simultaneously, so every value used
        // to assign and deal it (P/T, combat keywords, protection, prevention) comes from one
        // pre-damage snapshot captured under a single shared layered pass — mid-step mutations
        // (infect counters, consumed shields) must not feed back into this step's damage math.
        // Casualty determination below stays on live queries: it must see those mutations.
        DamagePhaseSnapshot snap = snapshotDamagePhase(gameData, blockerMap, atkBf, defBf);

        Map<Integer, Integer> blockerDamage = precomputeBlockerDamage(
                snap, blockerMap, state.deadDefenderIndices, isFirstStrikePhase);

        // Track remaining damage for multi-blocking creatures (CR 510.1c-d)
        Map<Integer, Integer> blockerRemainingDamage = new HashMap<>(blockerDamage);

        for (var entry : blockerMap.entrySet()) {
            int atkIdx = entry.getKey();
            List<Integer> blkIndices = entry.getValue();

            if (!isFirstStrikePhase && state.deadAttackerIndices.contains(atkIdx)) continue;

            Permanent atk = atkBf.get(atkIdx);
            CombatantStats atkStats = snap.attackerStats().get(atkIdx);
            boolean atkParticipates = atkStats.participatesInDamagePhase(isFirstStrikePhase);

            Map<UUID, Integer> playerAssignment = isFirstStrikePhase ? null
                    : gameData.combatDamagePlayerAssignments.get(atkIdx);

            boolean assignAsUnblocked = !blkIndices.isEmpty() && assignsCombatDamageAsThoughUnblocked(atk);

            if (playerAssignment != null) {
                if (atkParticipates && !atkStats.preventedFromDealingCombatDamage()) {
                    applyPlayerAssignedDamage(gameData, state, atk, atkStats, blkIndices, defBf,
                            playerAssignment, activeId, defenderId, redirectTarget, snap.damagePreventable());
                }
            } else if (blkIndices.isEmpty() || assignAsUnblocked) {
                if (atkParticipates && !atkStats.preventedFromDealingCombatDamage()) {
                    int power = gameQueryService.applyCombatDamageMultiplier(gameData, atkStats.combatDamage(), atk, null);
                    accumulatePlayerDamage(gameData, atk, atkStats, power, defenderId, redirectTarget, state);
                }
            } else {
                if (atkParticipates && !atkStats.preventedFromDealingCombatDamage()) {
                    distributeAttackerDamageToBlockers(gameData, state, atk, atkIdx, atkStats, blkIndices, defBf,
                            activeId, defenderId, redirectTarget, !isFirstStrikePhase, snap);
                }
            }

            if (!blkIndices.isEmpty()) {
                for (int blkIdx : blkIndices) {
                    if (!isFirstStrikePhase && state.deadDefenderIndices.contains(blkIdx)) continue;
                    Permanent blk = defBf.get(blkIdx);
                    CombatantStats blkStats = snap.defenderStats().get(blkIdx);
                    boolean blkParticipates = blkStats.participatesInDamagePhase(isFirstStrikePhase);
                    if (blkParticipates && !blkStats.preventedFromDealingCombatDamage()
                            && !(snap.damagePreventable() && snap.isAttackerProtectedFromBlocker(atkIdx, blkIdx))) {
                        // For multi-blocking creatures, distribute damage per CR 510.1c-d:
                        // assign lethal to each attacker in order before moving on
                        int blkTargetCount = blk.getBlockingTargets().size();
                        int assignedDmg;
                        if (blkTargetCount <= 1) {
                            assignedDmg = blockerDamage.getOrDefault(blkIdx, 0);
                        } else {
                            int blkRemaining = blockerRemainingDamage.getOrDefault(blkIdx, 0);
                            int atkDamageSoFar = atk.getMarkedDamage() + state.atkDamageTaken.getOrDefault(atkIdx, 0);
                            int lethalNeeded = blkStats.deathtouch()
                                    ? Math.max(0, 1 - atkDamageSoFar)
                                    : Math.max(0, atkStats.toughness() - atkDamageSoFar);
                            assignedDmg = Math.min(blkRemaining, lethalNeeded);
                            blockerRemainingDamage.put(blkIdx, blkRemaining - assignedDmg);
                        }
                        int actualDmg = gameQueryService.applyCombatDamageMultiplier(gameData, assignedDmg, blk, atk);
                        applyCombatCreatureDamage(gameData, blk, blkStats, atk, atkIdx, actualDmg, state.atkDamageTaken, state.deathtouchDamagedAttackerIndices);
                        state.combatDamageDealt.merge(blk, actualDmg, Integer::sum);
                        recordCombatDamageToCreature(gameData, state, blk, defenderId, atk, actualDmg);
                    }
                }
            }
        }

        determineCasualties(gameData, attackingIndices, atkBf, state.atkDamageTaken,
                state.deathtouchDamagedAttackerIndices, state.deadAttackerIndices, !isFirstStrikePhase);
        determineBlockerCasualties(gameData, blockerMap, defBf, state, !isFirstStrikePhase);
    }

    /**
     * Pre-damage view of one combat participant, captured before any combat damage of the
     * current damage step is applied (CR 510.4 — damage is dealt simultaneously, so assignment
     * and dealing use the pre-damage board even when earlier loop iterations placed infect
     * counters or removed permanents).
     */
    private record CombatantStats(int combatDamage, int toughness, boolean firstStrike,
                                  boolean doubleStrike, boolean deathtouch, boolean trample,
                                  boolean infect, boolean preventedFromDealingCombatDamage,
                                  CardColor color) {

        boolean participatesInDamagePhase(boolean isFirstStrikePhase) {
            return isFirstStrikePhase ? (firstStrike || doubleStrike) : (!firstStrike || doubleStrike);
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
                    if (gameQueryService.hasProtectionFromSource(gameData, atk, blk)) {
                        attackerProtectedFromBlocker.add(DamagePhaseSnapshot.pairKey(atkIdx, blkIdx));
                    }
                    if (gameQueryService.hasProtectionFromSource(gameData, blk, atk)) {
                        blockerProtectedFromAttacker.add(DamagePhaseSnapshot.pairKey(atkIdx, blkIdx));
                    }
                }
            }
            return new DamagePhaseSnapshot(attackerStats, defenderStats,
                    gameQueryService.isDamagePreventable(gameData),
                    attackerProtectedFromBlocker, blockerProtectedFromAttacker);
        });
    }

    private CombatantStats snapshotCombatant(GameData gameData, Permanent creature) {
        return new CombatantStats(
                gameQueryService.getEffectiveCombatDamage(gameData, creature),
                gameQueryService.getEffectiveToughness(gameData, creature),
                gameQueryService.hasKeyword(gameData, creature, Keyword.FIRST_STRIKE),
                gameQueryService.hasKeyword(gameData, creature, Keyword.DOUBLE_STRIKE),
                gameQueryService.hasKeyword(gameData, creature, Keyword.DEATHTOUCH),
                gameQueryService.hasKeyword(gameData, creature, Keyword.TRAMPLE),
                gameQueryService.hasKeyword(gameData, creature, Keyword.INFECT),
                gameQueryService.isPreventedFromDealingDamage(gameData, creature, true),
                gameQueryService.getEffectiveColor(gameData, creature));
    }

    private void distributeAttackerDamageToBlockers(GameData gameData, CombatDamageState state,
                                                     Permanent atk, int atkIdx, CombatantStats atkStats,
                                                     List<Integer> blkIndices,
                                                     List<Permanent> defBf,
                                                     UUID activeId, UUID defenderId,
                                                     Permanent redirectTarget,
                                                     boolean skipDeadBlockers, DamagePhaseSnapshot snap) {
        boolean atkHasDeathtouch = atkStats.deathtouch();
        int remaining = atkStats.combatDamage();
        for (int blkIdx : blkIndices) {
            if (skipDeadBlockers && state.deadDefenderIndices.contains(blkIdx)) continue;
            Permanent blk = defBf.get(blkIdx);
            int blockerDamageSoFar = blk.getMarkedDamage() + state.defDamageTaken.getOrDefault(blkIdx, 0);
            int lethalNeeded = atkHasDeathtouch
                    ? Math.max(0, 1 - blockerDamageSoFar)
                    : snap.defenderStats().get(blkIdx).toughness() - blockerDamageSoFar;
            int dmg = Math.min(remaining, Math.max(0, lethalNeeded));
            if (!(snap.damagePreventable() && snap.isBlockerProtectedFromAttacker(atkIdx, blkIdx))) {
                int actualDmg = gameQueryService.applyCombatDamageMultiplier(gameData, dmg, atk, blk);
                applyCombatCreatureDamage(gameData, atk, atkStats, blk, blkIdx, actualDmg, state.defDamageTaken, state.deathtouchDamagedDefenderIndices);
                state.combatDamageDealt.merge(atk, actualDmg, Integer::sum);
                recordCombatDamageToCreature(gameData, state, atk, activeId, blk, actualDmg);
            }
            remaining -= dmg;
        }
        if (remaining > 0 && atkStats.trample()) {
            int doubledRemaining = gameQueryService.applyCombatDamageMultiplier(gameData, remaining, atk, null);
            accumulatePlayerDamage(gameData, atk, atkStats, doubledRemaining, defenderId, redirectTarget, state);
        }
    }

    private void applyPlayerAssignedDamage(GameData gameData, CombatDamageState state,
                                            Permanent atk, CombatantStats atkStats, List<Integer> blkIndices,
                                            List<Permanent> defBf,
                                            Map<UUID, Integer> playerAssignment,
                                            UUID activeId, UUID defenderId,
                                            Permanent redirectTarget, boolean damagePreventable) {
        UUID overflowTargetId = atk.getAttackTarget() != null ? atk.getAttackTarget() : defenderId;
        for (var dmgEntry : playerAssignment.entrySet()) {
            UUID targetId = dmgEntry.getKey();
            int dmg = dmgEntry.getValue();
            if (dmg <= 0) continue;
            if (targetId.equals(overflowTargetId)) {
                // Overflow damage to the attack target (player or planeswalker)
                int actualDmg = gameQueryService.applyCombatDamageMultiplier(gameData, dmg, atk, null);
                accumulatePlayerDamage(gameData, atk, atkStats, actualDmg, defenderId, redirectTarget, state);
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
                    if (!(damagePreventable && gameQueryService.hasProtectionFromSource(gameData, blk, atk))) {
                        int actualDmg = gameQueryService.applyCombatDamageMultiplier(gameData, dmg, atk, blk);
                        applyCombatCreatureDamage(gameData, atk, atkStats, blk, targetIdx, actualDmg, state.defDamageTaken, state.deathtouchDamagedDefenderIndices);
                        state.combatDamageDealt.merge(atk, actualDmg, Integer::sum);
                        recordCombatDamageToCreature(gameData, state, atk, activeId, blk, actualDmg);
                    }
                }
            }
        }
    }

    private void resolveRedirectedDamage(GameData gameData, CombatDamageState state,
                                          Permanent redirectTarget) {
        if (redirectTarget != null && state.infectDamageRedirectedToGuard > 0) {
            state.infectDamageRedirectedToGuard = damagePreventionService.applyCreaturePreventionShield(gameData, redirectTarget, state.infectDamageRedirectedToGuard, true);
            if (state.infectDamageRedirectedToGuard > 0 && !gameQueryService.cantHaveCounters(gameData, redirectTarget)) {
                redirectTarget.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, redirectTarget.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + state.infectDamageRedirectedToGuard);
                String redirectLog = redirectTarget.getCard().getName() + " gets " + state.infectDamageRedirectedToGuard + " -1/-1 counters from redirected infect damage.";
                gameBroadcastService.logAndBroadcast(gameData, redirectLog);
            }
        }

        if (redirectTarget != null && (state.damageRedirectedToGuard > 0 || (state.infectDamageRedirectedToGuard > 0 && gameQueryService.getEffectiveToughness(gameData, redirectTarget) <= 0))) {
            state.damageRedirectedToGuard = damagePreventionService.applyCreaturePreventionShield(gameData, redirectTarget, state.damageRedirectedToGuard, true);
            if (state.damageRedirectedToGuard > 0) {
                String redirectLog = redirectTarget.getCard().getName() + " absorbs " + state.damageRedirectedToGuard + " redirected combat damage.";
                gameBroadcastService.logAndBroadcast(gameData, redirectLog);
            }

            int guardToughness = gameQueryService.getEffectiveToughness(gameData, redirectTarget);
            if (guardToughness <= 0) {
                permanentRemovalService.removePermanentToGraveyard(gameData, redirectTarget);
                String deathLog = redirectTarget.getCard().getName() + " dies from 0 toughness.";
                gameBroadcastService.logAndBroadcast(gameData, deathLog);
            } else if (gameQueryService.isLethalDamage(state.damageRedirectedToGuard, guardToughness, false)
                    && !gameQueryService.hasKeyword(gameData, redirectTarget, Keyword.INDESTRUCTIBLE)
                    && !graveyardService.tryRegenerate(gameData, redirectTarget)) {
                permanentRemovalService.removePermanentToGraveyard(gameData, redirectTarget);
                String deathLog = redirectTarget.getCard().getName() + " is destroyed by redirected combat damage.";
                gameBroadcastService.logAndBroadcast(gameData, deathLog);
            }
        }
    }


    private void processLifelink(GameData gameData, Map<Permanent, Integer> combatDamageDealt) {
        for (var entry : combatDamageDealt.entrySet()) {
            Permanent creature = entry.getKey();
            int damageDealt = entry.getValue();
            if (damageDealt <= 0) continue;
            if (!gameQueryService.hasKeyword(gameData, creature, Keyword.LIFELINK)) continue;
            UUID controllerId = CombatHelper.findControllerOf(gameData, creature);
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
                            lifeSupport.applyGainLife(gameData, playerId, damageDealt, perm.getCard().getName());
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
            UUID controllerId = CombatHelper.findControllerOf(gameData, creature);
            if (controllerId == null) continue;
            // "Whenever a creature you control deals combat damage, you gain that much life."
            // Fires once per matching enchantment the creature's controller controls (Noble Purpose).
            gameData.forEachPermanent((ownerId, perm) -> {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof GainLifeEqualToControlledCreatureCombatDamageEffect) {
                        UUID enchantmentControllerId = gameQueryService.findPermanentController(gameData, perm.getId());
                        if (controllerId.equals(enchantmentControllerId)) {
                            lifeSupport.applyGainLife(gameData, controllerId, damageDealt, perm.getCard().getName());
                        }
                    }
                }
            });
        }
    }

    private void processCombatDamageToPlayerTriggers(GameData gameData, Map<Permanent, Integer> combatDamageDealtToPlayer, UUID attackerId, UUID defenderId) {
        for (var entry : combatDamageDealtToPlayer.entrySet()) {
            Permanent creature = entry.getKey();
            int damageDealt = entry.getValue();
            if (damageDealt <= 0) continue;

            gameData.combatDamageToPlayersThisTurn
                    .computeIfAbsent(creature.getId(), k -> ConcurrentHashMap.newKeySet())
                    .add(defenderId);

            // Record creature subtypes at combat damage time for subtype-conditional triggers
            // (e.g. Admiral Beckett Brass checks if 3+ Pirates dealt damage to a player)
            if (!gameData.combatDamageSourceSubtypesThisTurn.containsKey(creature.getId())) {
                Set<CardSubtype> effectiveSubtypes = ConcurrentHashMap.newKeySet();
                effectiveSubtypes.addAll(creature.getCard().getSubtypes());
                effectiveSubtypes.addAll(creature.getGrantedSubtypes());
                effectiveSubtypes.addAll(creature.getTransientSubtypes());
                gameData.combatDamageSourceSubtypesThisTurn.put(creature.getId(), effectiveSubtypes);
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
            if (gameData.combatDamageSourcesWithChangelingThisTurn.contains(creature.getId())) {
                gameData.controllersDealtCombatDamageWithChangelingThisTurn.add(attackerId);
            }

            List<CardEffect> allDamageEffects = new ArrayList<>();
            allDamageEffects.addAll(creature.getCard().getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER));
            allDamageEffects.addAll(creature.getCard().getEffects(EffectSlot.ON_DAMAGE_TO_PLAYER));
            for (CardEffect effect : allDamageEffects) {
                if (effect instanceof ConditionalEffect metalcraft && metalcraft.condition() instanceof Metalcraft) {
                    if (!conditionEvaluationService.isMet(gameData, metalcraft.condition(),
                            ConditionContext.forPermanent(creature, attackerId))) {
                        log.info("Game {} - {}'s metalcraft combat damage trigger does not fire",
                                gameData.id, creature.getCard().getName());
                        continue;
                    }
                    StackEntry se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            creature.getCard().getName() + "'s triggered ability",
                            List.of(metalcraft), defenderId, creature.getId());
                    se.setNonTargeting(true);
                    gameData.stack.add(se);
                    gameBroadcastService.logAndBroadcast(gameData, creature.getCard().getName() + "'s metalcraft ability triggers:");
                    continue;
                }

                if (effect instanceof TargetPlayerLosesGameEffect) {
                    gameData.stack.add(new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            creature.getCard().getName() + "'s triggered ability",
                            List.of(new TargetPlayerLosesGameEffect(defenderId)), null, creature.getId()));
                    gameBroadcastService.logAndBroadcast(gameData,
                            creature.getCard().getName() + "'s ability triggers \u2014 " + gameData.playerIdToName.get(defenderId) + " loses the game.");
                    continue;
                }

                if (effect instanceof MayEffect may) {
                    if (may.wrapped() instanceof SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect
                            || may.wrapped() instanceof TransformSelfAndAttachToCreatureDamagedPlayerControlsEffect) {
                        List<Permanent> defenderBf = gameData.playerBattlefields.get(defenderId);
                        boolean hasCreatureTargets = false;
                        if (defenderBf != null) {
                            for (Permanent p : defenderBf) {
                                if (gameQueryService.isCreature(gameData, p)) {
                                    hasCreatureTargets = true;
                                    break;
                                }
                            }
                        }
                        if (!hasCreatureTargets) {
                            gameBroadcastService.logAndBroadcast(gameData, creature.getCard().getName()
                                    + "'s ability does not trigger — " + gameData.playerIdToName.get(defenderId) + " has no creatures.");
                            continue;
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
                            gameBroadcastService.logAndBroadcast(gameData, creature.getCard().getName()
                                    + "'s ability does not trigger — " + gameData.playerIdToName.get(defenderId) + " has no valid targets.");
                            continue;
                        }
                    }
                    // Wire the combat damage dealt as the event value so a wrapped "draw that many
                    // cards" (DrawCardEffect with an EventValue amount, e.g. Cold-Eyed Selkie) reads it.
                    int mayEventValue = may.wrapped() instanceof DrawCardEffect draw
                            && draw.amount() instanceof EventValue ? damageDealt : 0;
                    gameData.queueMayAbility(creature.getCard(), attackerId, may, defenderId, creature.getId(), mayEventValue);
                    gameBroadcastService.logAndBroadcast(gameData, creature.getCard().getName() + "'s combat damage trigger fires.");
                    continue;
                }

                if (effect instanceof DestroyPermanentDamagedPlayerControlsEffect destroyEffect) {
                    if (damageDealt < destroyEffect.minimumDamage()) {
                        gameBroadcastService.logAndBroadcast(gameData, creature.getCard().getName()
                                + "'s ability does not trigger — less than " + destroyEffect.minimumDamage()
                                + " damage dealt.");
                        continue;
                    }
                    List<Permanent> defenderBf = gameData.playerBattlefields.get(defenderId);
                    boolean hasValidTargets = false;
                    if (defenderBf != null) {
                        for (Permanent p : defenderBf) {
                            if (destroyEffect.predicate() == null
                                    || predicateEvaluationService.matchesPermanentPredicate(gameData, p, destroyEffect.predicate())) {
                                hasValidTargets = true;
                                break;
                            }
                        }
                    }
                    if (!hasValidTargets) {
                        gameBroadcastService.logAndBroadcast(gameData, creature.getCard().getName()
                                + "'s ability does not trigger — " + gameData.playerIdToName.get(defenderId)
                                + " has no valid targets.");
                        continue;
                    }
                    StackEntry destroySe = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            creature.getCard().getName() + "'s triggered ability", List.of(effect), defenderId, creature.getId());
                    destroySe.setNonTargeting(true);
                    gameData.stack.add(destroySe);
                    gameBroadcastService.logAndBroadcast(gameData, creature.getCard().getName() + "'s combat damage trigger goes on the stack.");
                    continue;
                }

                if (effect instanceof SacrificePermanentDamagedPlayerControlsEffect sacrificeEffect) {
                    if (damageDealt < sacrificeEffect.minimumDamage()) {
                        gameBroadcastService.logAndBroadcast(gameData, creature.getCard().getName()
                                + "'s ability does not trigger — less than " + sacrificeEffect.minimumDamage()
                                + " damage dealt.");
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
                        gameBroadcastService.logAndBroadcast(gameData, creature.getCard().getName()
                                + "'s ability does not trigger — " + gameData.playerIdToName.get(defenderId)
                                + " has no valid targets.");
                        continue;
                    }
                    StackEntry sacrificeSe = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            creature.getCard().getName() + "'s triggered ability", List.of(effect), defenderId, creature.getId());
                    sacrificeSe.setNonTargeting(true);
                    gameData.stack.add(sacrificeSe);
                    gameBroadcastService.logAndBroadcast(gameData, creature.getCard().getName() + "'s combat damage trigger goes on the stack.");
                    continue;
                }

                String desc = creature.getCard().getName() + "'s triggered ability";
                StackEntry se;
                if (effect instanceof ReturnPermanentsOnCombatDamageToPlayerEffect
                        || effect instanceof DealDamageToEachCreatureDamagedPlayerControlsEffect
                        || effect instanceof LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect) {
                    se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            desc, List.of(effect), damageDealt, defenderId, null);
                } else if (effect instanceof PutCountersOnSourceEffect
                        || effect instanceof RemoveAllCountersFromSelfEffect
                        || effect instanceof ExploreEffect) {
                    se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            desc, List.of(effect), null, creature.getId());
                } else if (effect instanceof ExileTopCardsRepeatOnDuplicateEffect
                        || (effect instanceof DiscardEffect discardEffect
                                && discardEffect.recipient() == DiscardRecipient.TARGET_PLAYER)
                        || effect instanceof TargetPlayerRandomDiscardOrControllerDrawsEffect
                        || effect instanceof RevealRandomCardFromTargetPlayerHandEffect
                        || effect instanceof SphinxAmbassadorEffect
                        || (effect instanceof MillEffect mill && mill.recipient() == MillRecipient.TARGET_PLAYER)
                        || effect instanceof TargetPlayerExilesFromHandEffect
                        || effect instanceof ChooseCardsFromTargetHandEffect
                        || effect instanceof SkipNextCombatPhaseEffect
                        || effect instanceof TargetPlayerCantGainLifeRestOfGameEffect
                        || (effect instanceof DealDamageToPlayersEffect dmg && dmg.recipient() == DamageRecipient.TARGET_PLAYER)) {
                    se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            desc, List.of(effect), defenderId, creature.getId());
                } else {
                    se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            desc, List.of(effect));
                }
                // Wire the combat damage dealt as the event value so "discards that many cards"
                // (DiscardEffect with an EventValue amount, e.g. Needle Specter) reads it.
                if (effect instanceof DiscardEffect) {
                    se.setEventValue(damageDealt);
                }
                se.setNonTargeting(true);
                gameData.stack.add(se);
                gameBroadcastService.logAndBroadcast(gameData, creature.getCard().getName() + "'s combat damage trigger goes on the stack.");
            }

            if (creature.isHasDamageToOpponentCreatureBounce()) {
                String desc = creature.getCard().getName() + "'s triggered ability";
                StackEntry bounceSe = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                        desc, List.of(new ReturnPermanentsOnCombatDamageToPlayerEffect(new PermanentIsCreaturePredicate())), 1, defenderId, null);
                bounceSe.setNonTargeting(true);
                gameData.stack.add(bounceSe);
                gameBroadcastService.logAndBroadcast(gameData, creature.getCard().getName() + "'s damage-to-opponent bounce trigger goes on the stack.");
            }

            checkAttachedCombatDamageToPlayerTriggers(gameData, creature, attackerId, defenderId);
            checkPlayerAttachedCurseCombatDamageTriggers(gameData, creature, attackerId, defenderId);
            checkAllyCreatureCombatDamageToPlayerTriggers(gameData, creature, attackerId, defenderId);
        }
    }

    private void checkAttachedCombatDamageToPlayerTriggers(GameData gameData, Permanent creature, UUID attackerId, UUID defenderId) {
        gameData.forEachPermanent((ownerId, perm) -> {
            if (perm.isAttached() && perm.getAttachedTo().equals(creature.getId())) {
                List<CardEffect> rawEffects = new ArrayList<>();
                rawEffects.addAll(perm.getCard().getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER));
                rawEffects.addAll(perm.getCard().getEffects(EffectSlot.ON_DAMAGE_TO_PLAYER));

                // Unwrap EnchantedPermanentConditionalEffect against the enchanted creature that
                // dealt the damage, so a granted trigger gated on the creature's characteristics
                // (e.g. Helm of the Ghastlord's colour-conditional draw/discard) only fires on the
                // matching branch. Null branches contribute nothing.
                List<CardEffect> effects = new ArrayList<>();
                for (CardEffect effect : rawEffects) {
                    if (effect instanceof EnchantedPermanentConditionalEffect cond) {
                        CardEffect active = predicateEvaluationService.matchesPermanentPredicate(gameData, creature, cond.filter())
                                ? cond.ifMatch()
                                : cond.ifNotMatch();
                        if (active != null) {
                            effects.add(active);
                        }
                    } else {
                        effects.add(effect);
                    }
                }

                if (!effects.isEmpty()) {
                    StackEntry se = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            attackerId,
                            perm.getCard().getName() + "'s triggered ability",
                            new ArrayList<>(effects),
                            defenderId,
                            perm.getId()
                    );
                    se.setNonTargeting(true);
                    gameData.stack.add(se);
                    String logEntry = perm.getCard().getName() + "'s combat damage trigger goes on the stack.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                }
            }
        });
    }

    /**
     * Checks for curse enchantments attached to the defending player that trigger on combat damage.
     * E.g. Curse of Stalked Prey: "Whenever a creature deals combat damage to enchanted player,
     * put a +1/+1 counter on that creature."
     */
    private void checkPlayerAttachedCurseCombatDamageTriggers(GameData gameData, Permanent creature, UUID attackerId, UUID defenderId) {
        gameData.forEachPermanent((ownerId, perm) -> {
            if (perm.isAttached() && perm.getAttachedTo().equals(defenderId)) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER);
                for (CardEffect effect : effects) {
                    if (effect instanceof PutCountersOnSourceEffect) {
                        // "sourcePermanentId" is set to the creature that dealt damage, so counters go on it
                        StackEntry se = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                ownerId,
                                perm.getCard().getName() + "'s triggered ability",
                                List.of(effect),
                                null,
                                creature.getId()
                        );
                        se.setNonTargeting(true);
                        gameData.stack.add(se);
                        gameBroadcastService.logAndBroadcast(gameData, perm.getCard().getName()
                                + "'s combat damage trigger goes on the stack.");
                    }
                }
            }
        });
    }

    /**
     * Checks for permanents the attacker controls that trigger when an ally creature deals
     * combat damage to a player. E.g. Rakish Heir: "Whenever a Vampire you control deals
     * combat damage to a player, put a +1/+1 counter on it."
     */
    private void checkAllyCreatureCombatDamageToPlayerTriggers(GameData gameData, Permanent creature, UUID attackerId, UUID defenderId) {
        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(attackerId);
        if (attackerBattlefield == null) return;

        for (Permanent perm : attackerBattlefield) {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER);
            for (CardEffect effect : effects) {
                if (effect instanceof AllyCombatDamageTriggerEffect trigger) {
                    if (trigger.dealerPredicate() != null
                            && !predicateEvaluationService.matchesPermanentPredicate(gameData, creature, trigger.dealerPredicate())) {
                        continue;
                    }
                    // Bind the damaged player so effects like DiscardEffect(TARGET_PLAYER) resolve
                    // against them (Oona's Blackguard: "...that player discards a card").
                    StackEntry se = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            attackerId,
                            perm.getCard().getName() + "'s triggered ability",
                            List.of(trigger.effect()),
                            defenderId,
                            trigger.bindSourceToDealer() ? creature.getId() : perm.getId()
                    );
                    se.setNonTargeting(true);
                    gameData.stack.add(se);
                    gameBroadcastService.logAndBroadcast(gameData, perm.getCard().getName()
                            + "'s triggered ability goes on the stack.");
                }
            }
        }

        // Graveyard-based variant (GRAVEYARD_ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER): the trigger
        // fires from the attacker's graveyard. E.g. Auntie's Snitch — "Whenever a Goblin or Rogue you
        // control deals combat damage to a player, if this card is in your graveyard, you may return
        // this card to your hand." The stack entry's source is the graveyard card itself.
        List<Card> graveyard = gameData.playerGraveyards.get(attackerId);
        if (graveyard == null) return;
        for (Card card : new ArrayList<>(graveyard)) {
            for (CardEffect effect : card.getEffects(EffectSlot.GRAVEYARD_ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER)) {
                if (effect instanceof AllyCombatDamageTriggerEffect trigger) {
                    if (trigger.dealerPredicate() != null
                            && !predicateEvaluationService.matchesPermanentPredicate(gameData, creature, trigger.dealerPredicate())) {
                        continue;
                    }
                    StackEntry se = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            card,
                            attackerId,
                            card.getName() + "'s graveyard trigger",
                            List.of(trigger.effect())
                    );
                    se.setNonTargeting(true);
                    gameData.stack.add(se);
                    gameBroadcastService.logAndBroadcast(gameData, card.getName()
                            + "'s graveyard trigger goes on the stack.");
                }
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
                        List.of(new DrawAndDiscardCardEffect(loot.drawAmount(), loot.discardAmount()))
                );
                se.setNonTargeting(true);
                gameData.stack.add(se);
                gameBroadcastService.logAndBroadcast(gameData,
                        loot.sourceCard().getName() + "'s delayed trigger fires — draw " + loot.drawAmount()
                                + ", discard " + loot.discardAmount() + ".");
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
                        List.of(new DealDamageToPlayersEffect(damage, DamageRecipient.TARGET_PLAYER)),
                        attackerId,
                        attacker.getId());
                se.setNonTargeting(true);
                gameData.stack.add(se);
                gameBroadcastService.logAndBroadcast(gameData, reflection.sourceCard().getName()
                        + " reflects " + damage + " combat damage from " + attacker.getCard().getName()
                        + " to " + gameData.playerIdToName.get(attackerId) + ".");
            }
        }
    }

    private void processCombatDamageToCreatureTriggers(GameData gameData,
                                                        Map<Permanent, List<UUID>> combatDamageDealtToCreatures,
                                                        Map<Permanent, UUID> combatDamageDealerControllers) {
        for (var entry : combatDamageDealtToCreatures.entrySet()) {
            Permanent source = entry.getKey();
            UUID controllerId = combatDamageDealerControllers.get(source);
            if (controllerId == null) continue;

            List<CardEffect> effects = source.getCard().getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE);
            if (effects.isEmpty()) continue;

            for (UUID damagedCreatureId : entry.getValue()) {
                for (CardEffect effect : effects) {
                    CardEffect effectToUse = null;
                    if (effect instanceof DestroyTargetPermanentEffect destroyEffect) {
                        effectToUse = destroyEffect;
                    } else if (effect instanceof FlipCoinWinEffect flipEffect) {
                        effectToUse = flipEffect;
                    }
                    if (effectToUse != null) {
                        StackEntry trigger = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                source.getCard(),
                                controllerId,
                                source.getCard().getName() + "'s triggered ability",
                                List.of(effectToUse),
                                damagedCreatureId,
                                source.getId()
                        );
                        trigger.setNonTargeting(true);
                        gameData.stack.add(trigger);
                        String logEntry = source.getCard().getName() + "'s ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    }
                }
            }
        }
    }

    /**
     * Fires ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE reflection triggers (e.g. Greatbow Doyen) for
     * each source/target combat-damage pair. Each source creature that dealt damage to a creature
     * this step reflects that damage to the damaged creature's controller if a watcher listens.
     */
    private void processAllyDealtDamageToCreatureReflectionTriggers(GameData gameData, CombatDamageState state) {
        for (var entry : state.combatDamageAmountsToCreatures.entrySet()) {
            Permanent source = entry.getKey();
            UUID sourceControllerId = state.combatDamageDealerControllers.get(source);
            if (sourceControllerId == null) continue;
            for (var amountEntry : entry.getValue().entrySet()) {
                UUID damagedCreatureControllerId = state.combatDamageTargetControllers.get(amountEntry.getKey());
                triggerCollectionService.checkAllyDealtDamageToCreatureTriggers(
                        gameData, source, sourceControllerId, damagedCreatureControllerId, amountEntry.getValue());
            }
        }
    }

    /**
     * Fires ON_ANY_CREATURE_DEALT_DAMAGE triggers (e.g. Death Pits of Rath) for each creature that
     * took combat damage this step. Creatures already removed by lethal combat damage are skipped —
     * they're gone; only surviving damaged creatures are looked up and passed to the trigger scan.
     */
    private void processAnyCreatureDealtDamageTriggers(GameData gameData, CombatDamageState state) {
        Set<UUID> damagedCreatureIds = new LinkedHashSet<>();
        for (var entry : state.combatDamageDealtToCreatures.entrySet()) {
            Map<UUID, Integer> amounts = state.combatDamageAmountsToCreatures.getOrDefault(entry.getKey(), Map.of());
            for (UUID targetId : entry.getValue()) {
                if (amounts.getOrDefault(targetId, 0) > 0) {
                    damagedCreatureIds.add(targetId);
                }
            }
        }
        for (UUID id : damagedCreatureIds) {
            Permanent damaged = gameQueryService.findPermanentById(gameData, id);
            if (damaged == null) continue;
            triggerCollectionService.checkAnyCreatureDealtDamageTriggers(gameData, damaged);
        }
    }

    private record DealtDamageTriggerData(Card card, UUID permanentId, UUID controllerId, int damageDealt, UUID sourceControllerId) {}

    private List<DealtDamageTriggerData> collectDealtDamageTriggerData(GameData gameData, CombatDamageState state) {
        List<DealtDamageTriggerData> triggers = new ArrayList<>();
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
                List<CardEffect> effects = target.getCard().getEffects(EffectSlot.ON_DEALT_DAMAGE);
                if (effects.isEmpty()) continue;
                UUID controllerId = CombatHelper.findControllerOf(gameData, target);
                if (controllerId == null) continue;
                int damageAmount = damageAmounts.getOrDefault(targetId, 0);
                triggers.add(new DealtDamageTriggerData(target.getCard(), target.getId(), controllerId, damageAmount, sourceControllerId));
            }
        }
        return triggers;
    }

    private void processDealtDamageTriggers(GameData gameData, List<DealtDamageTriggerData> triggerData) {
        for (DealtDamageTriggerData data : triggerData) {
            for (CardEffect effect : data.card().getEffects(EffectSlot.ON_DEALT_DAMAGE)) {
                CardEffect effectToAdd = effect;
                if (effect instanceof DamageSourceControllerSacrificesPermanentsEffect && data.damageDealt() > 0 && data.sourceControllerId() != null) {
                    effectToAdd = new DamageSourceControllerSacrificesPermanentsEffect(data.damageDealt(), data.sourceControllerId());
                } else if (effect instanceof DamageSourceControllerGetsPoisonCounterEffect && data.sourceControllerId() != null) {
                    effectToAdd = new DamageSourceControllerGetsPoisonCounterEffect(data.sourceControllerId());
                } else if (effect instanceof DealDamageToTargetOpponentOrPlaneswalkerEffect) {
                    // Targeting effect — auto-target opponent when no planeswalkers, otherwise queue for choice
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
                                null,
                                data.permanentId()
                        );
                        entry.setTargetId(opponentId);
                        gameData.stack.add(entry);
                    } else {
                        gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                                data.card(), data.controllerId(), new ArrayList<>(List.of(effectToAdd)), false, null
                        ));
                    }
                    String logEntry = data.card().getName() + "'s ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} ON_DEALT_DAMAGE combat trigger fires", gameData.id, data.card().getName());
                    continue;
                } else if (effect instanceof DealDamageToAnyTargetEffect) {
                    // "It deals that much damage to any target" (Spitemare): the damage amount
                    // snapshots into xValue, and the controller chooses any target when serviced.
                    if (data.damageDealt() > 0) {
                        gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                                data.card(), data.controllerId(), new ArrayList<>(List.of(effect)),
                                false, null, data.damageDealt()));
                    }
                    String logEntry = data.card().getName() + "'s ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} ON_DEALT_DAMAGE combat trigger fires", gameData.id, data.card().getName());
                    continue;
                }
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        data.card(),
                        data.controllerId(),
                        data.card().getName() + "'s ability",
                        new ArrayList<>(List.of(effectToAdd)),
                        null,
                        data.permanentId()
                ));
                String logEntry = data.card().getName() + "'s ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} ON_DEALT_DAMAGE combat trigger fires", gameData.id, data.card().getName());
            }
        }
    }


    /**
     * Updates markedDamage on creatures from combat damage maps (CR 704.5g).
     * Called after determineCasualties so the prevention-applied values are final.
     */
    private void updateMarkedDamageFromCombat(GameData gameData, List<Permanent> atkBf, List<Permanent> defBf,
                                               CombatDamageState state) {
        for (var entry : state.atkDamageTaken.entrySet()) {
            int idx = entry.getKey();
            int dmg = entry.getValue();
            if (dmg > 0 && idx < atkBf.size()) {
                Permanent atk = atkBf.get(idx);
                atk.setMarkedDamage(atk.getMarkedDamage() + dmg);
                gameData.permanentsDealtDamageThisTurn.add(atk.getId());
            }
        }
        for (var entry : state.defDamageTaken.entrySet()) {
            int idx = entry.getKey();
            int dmg = entry.getValue();
            if (dmg > 0 && idx < defBf.size()) {
                Permanent def = defBf.get(idx);
                def.setMarkedDamage(def.getMarkedDamage() + dmg);
                gameData.permanentsDealtDamageThisTurn.add(def.getId());
            }
        }
    }

    private List<String> removeDeadCreatures(GameData gameData, CombatDamageState state,
                                              List<Permanent> atkBf, List<Permanent> defBf,
                                              UUID activeId, UUID defenderId) {
        List<String> deadCreatureNames = new ArrayList<>();
        // Collect dead attacker references and IDs before removal
        Set<UUID> deadAttackerIds = new HashSet<>();
        List<Permanent> deadAttackers = new ArrayList<>();
        for (int idx : state.deadAttackerIndices) {
            Permanent dead = atkBf.get(idx);
            deadAttackerIds.add(dead.getId());
            deadAttackers.add(dead);
        }
        List<Permanent> deadDefenders = new ArrayList<>();
        for (int idx : state.deadDefenderIndices) {
            deadDefenders.add(defBf.get(idx));
        }
        for (Permanent dead : deadAttackers) {
            deadCreatureNames.add(gameData.playerIdToName.get(activeId) + "'s " + dead.getCard().getName());
            permanentRemovalService.removePermanentToGraveyard(gameData, dead);
        }
        for (Permanent dead : deadDefenders) {
            deadCreatureNames.add(gameData.playerIdToName.get(defenderId) + "'s " + dead.getCard().getName());
            permanentRemovalService.removePermanentToGraveyard(gameData, dead);
        }
        // Clear blocking state for surviving blockers whose blocked attacker died
        clearOrphanedBlockingState(defBf, deadAttackerIds);
        permanentRemovalService.removeOrphanedAuras(gameData);
        return deadCreatureNames;
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


    private void applyPlayerDamage(GameData gameData, CombatDamageState state, UUID defenderId) {
        // Curse of Bloodletting and similar: double combat damage dealt to the enchanted player (replacement effect)
        int playerMultiplier = gameQueryService.getEnchantedPlayerDamageMultiplier(gameData, defenderId);
        state.damageToDefendingPlayer *= playerMultiplier;
        state.poisonDamageToDefendingPlayer *= playerMultiplier;
        // Deep Wood: prevent all damage attacking creatures would deal to the defending player this turn.
        if (damagePreventionService.isCombatDamageFromAttackersPreventedForPlayer(gameData, defenderId)) {
            state.damageToDefendingPlayer = 0;
            state.poisonDamageToDefendingPlayer = 0;
        }
        state.damageToDefendingPlayer = damagePreventionService.applyPlayerPreventionShield(gameData, defenderId, state.damageToDefendingPlayer);
        processPendingRedirectDamage(gameData);
        state.damageToDefendingPlayer = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, defenderId, state.damageToDefendingPlayer, "combat", true);
        // Phyrexian Unlife: convert normal combat damage to poison when at 0 or less life.
        // Uses pre-lifelink snapshot (CR 510.1: all combat damage is simultaneous).
        if (state.damageToDefendingPlayer > 0 && state.defenderDamageAsInfect) {
            state.poisonDamageToDefendingPlayer += state.damageToDefendingPlayer;
            state.damageToDefendingPlayer = 0;
        }
        if (state.damageToDefendingPlayer > 0) {
            if (gameQueryService.canPlayerLifeChange(gameData, defenderId)) {
                int currentLife = gameData.getLife(defenderId);
                int newLife = currentLife - state.damageToDefendingPlayer;
                // Worship: combat damage can't reduce the controller's life total below 1 while they
                // control a creature. The full damage is still dealt; only the life reduction is capped.
                if (currentLife >= 1 && newLife < 1
                        && gameQueryService.damageCantReduceLifeBelowOne(gameData, defenderId)) {
                    newLife = 1;
                }
                gameData.playerLifeTotals.put(defenderId, newLife);
                int lifeLost = currentLife - newLife;
                String logEntry = gameData.playerIdToName.get(defenderId) + " takes " + state.damageToDefendingPlayer + " combat damage.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                if (lifeLost > 0) {
                    triggerCollectionService.checkLifeLossTriggers(gameData, defenderId, lifeLost);
                }
            } else {
                gameBroadcastService.logAndBroadcast(gameData,
                        gameData.playerIdToName.get(defenderId) + "'s life total can't change.");
            }
        }

        state.poisonDamageToDefendingPlayer = damagePreventionService.applyPlayerPreventionShield(gameData, defenderId, state.poisonDamageToDefendingPlayer);
        if (state.poisonDamageToDefendingPlayer > 0 && gameQueryService.canPlayerGetPoisonCounters(gameData, defenderId)) {
            int currentPoison = gameData.playerPoisonCounters.getOrDefault(defenderId, 0);
            gameData.playerPoisonCounters.put(defenderId, currentPoison + state.poisonDamageToDefendingPlayer);
            String logEntry = gameData.playerIdToName.get(defenderId) + " gets " + state.poisonDamageToDefendingPlayer + " poison counters.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }

        // Track that the defending player was dealt damage this turn (for Bloodcrazed Goblin etc.)
        if (state.damageToDefendingPlayer > 0 || state.poisonDamageToDefendingPlayer > 0) {
            gameData.recordDamageToPlayer(defenderId,
                    state.damageToDefendingPlayer + state.poisonDamageToDefendingPlayer);
        }
    }

    /**
     * Processes pending redirect damage entries populated by {@link DamagePreventionService}
     * when damage redirect shields (e.g. Vengeful Archon) prevent damage.
     */
    private void processPendingRedirectDamage(GameData gameData) {
        if (gameData.pendingRedirectDamage.isEmpty()) return;

        List<DamageRedirectShield> toProcess = new ArrayList<>(gameData.pendingRedirectDamage);
        gameData.pendingRedirectDamage.clear();

        for (DamageRedirectShield redirect : toProcess) {
            UUID targetId = redirect.redirectTargetPlayerId();
            int damage = redirect.remainingAmount();
            String targetName = gameData.playerIdToName.get(targetId);
            String protectedName = gameData.playerIdToName.get(redirect.protectedPlayerId());

            gameBroadcastService.logAndBroadcast(gameData,
                    redirect.sourceCard().getName() + " prevents " + damage + " damage to " + protectedName + ".");
            gameBroadcastService.logAndBroadcast(gameData,
                    redirect.sourceCard().getName() + " deals " + damage + " damage to " + targetName + ".");

            int redirectEffective = damagePreventionService.applyPlayerPreventionShield(gameData, targetId, damage);
            processPendingRedirectDamage(gameData);

            if (redirectEffective > 0) {
                if (gameQueryService.canPlayerLifeChange(gameData, targetId)) {
                    int currentLife = gameData.getLife(targetId);
                    gameData.playerLifeTotals.put(targetId, currentLife - redirectEffective);
                }
                gameData.recordDamageToPlayer(targetId, redirectEffective);
            }
        }
    }

    private void applyPlaneswalkerDamage(GameData gameData, CombatDamageState state) {
        for (var entry : state.damageToPlaneswalkers.entrySet()) {
            UUID pwId = entry.getKey();
            int damage = entry.getValue();
            if (damage <= 0) continue;
            Permanent pw = gameQueryService.findPermanentById(gameData, pwId);
            if (pw == null) continue; // planeswalker may have left battlefield
            // CR 306.8: Damage dealt to a planeswalker removes that many loyalty counters from it
            pw.setCounterCount(CounterType.LOYALTY, pw.getCounterCount(CounterType.LOYALTY) - damage);
            String logEntry = pw.getCard().getName() + " takes " + damage + " combat damage ("
                    + pw.getCounterCount(CounterType.LOYALTY) + " loyalty remaining).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
        }
    }

    /**
     * Processes pending source-specific redirect damage entries (e.g. Harm's Way).
     * The prevented damage is dealt to the redirect target, which can be a player or permanent.
     */
    private void processSourceRedirectDamage(GameData gameData) {
        if (gameData.pendingSourceRedirectDamage.isEmpty()) return;

        List<SourceDamageRedirectShield> toProcess = new ArrayList<>(gameData.pendingSourceRedirectDamage);
        gameData.pendingSourceRedirectDamage.clear();

        for (SourceDamageRedirectShield redirect : toProcess) {
            UUID targetId = redirect.redirectTargetId();
            int damage = redirect.remainingAmount();
            boolean targetIsPlayer = gameData.playerIds.contains(targetId);

            if (targetIsPlayer) {
                String targetName = gameData.playerIdToName.get(targetId);
                gameBroadcastService.logAndBroadcast(gameData,
                        damage + " damage is redirected to " + targetName + ".");

                int redirectEffective = damagePreventionService.applyPlayerPreventionShield(gameData, targetId, damage);
                processPendingRedirectDamage(gameData);

                if (redirectEffective > 0) {
                    if (gameQueryService.canPlayerLifeChange(gameData, targetId)) {
                        int currentLife = gameData.getLife(targetId);
                        gameData.playerLifeTotals.put(targetId, currentLife - redirectEffective);
                    }
                    gameData.recordDamageToPlayer(targetId, redirectEffective);
                }
            } else {
                Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetId);
                if (targetPerm == null) continue;

                gameBroadcastService.logAndBroadcast(gameData,
                        damage + " damage is redirected to " + targetPerm.getCard().getName() + ".");

                int effectiveDamage = damagePreventionService.applyCreaturePreventionShield(gameData, targetPerm, damage, true);
                if (effectiveDamage > 0) {
                    targetPerm.setMarkedDamage(targetPerm.getMarkedDamage() + effectiveDamage);
                    gameData.permanentsDealtDamageThisTurn.add(targetPerm.getId());
                    int effToughness = gameQueryService.getEffectiveToughness(gameData, targetPerm);
                    if (gameQueryService.isLethalDamage(targetPerm.getMarkedDamage(), effToughness, false)
                            && !gameQueryService.hasKeyword(gameData, targetPerm, Keyword.INDESTRUCTIBLE)) {
                        permanentRemovalService.removePermanentToGraveyard(gameData, targetPerm);
                    }
                }
            }
        }
    }


    private void determineCasualties(GameData gameData, List<Integer> indices,
                                      List<Permanent> battlefield, Map<Integer, Integer> damageTaken,
                                      Set<Integer> deathtouchSet, Set<Integer> deadSet,
                                      boolean skipAlreadyDead) {
        for (int idx : indices) {
            if (skipAlreadyDead && deadSet.contains(idx)) continue;
            int dmg = damageTaken.getOrDefault(idx, 0);
            dmg = damagePreventionService.applyCreaturePreventionShield(gameData, battlefield.get(idx), dmg, true);
            damageTaken.put(idx, dmg);
            int effToughness = gameQueryService.getEffectiveToughness(gameData, battlefield.get(idx));
            if (effToughness <= 0) {
                deadSet.add(idx);
            } else if (gameQueryService.isLethalDamage(battlefield.get(idx).getMarkedDamage() + dmg, effToughness, deathtouchSet.contains(idx))
                    && !gameQueryService.hasKeyword(gameData, battlefield.get(idx), Keyword.INDESTRUCTIBLE)
                    && !graveyardService.tryRegenerate(gameData, battlefield.get(idx))) {
                deadSet.add(idx);
            }
        }
    }

    private void determineBlockerCasualties(GameData gameData, Map<Integer, List<Integer>> blockerMap,
                                             List<Permanent> defBf, CombatDamageState state,
                                             boolean skipAlreadyDead) {
        Set<Integer> seen = new LinkedHashSet<>();
        for (List<Integer> blkIndices : blockerMap.values()) {
            seen.addAll(blkIndices);
        }
        // Include any defending creature that took assigned combat damage without blocking
        // (e.g. an unblocked Cunning Giant redirecting its damage to a defending creature).
        seen.addAll(state.defDamageTaken.keySet());
        determineCasualties(gameData, new ArrayList<>(seen), defBf, state.defDamageTaken,
                state.deathtouchDamagedDefenderIndices, state.deadDefenderIndices, skipAlreadyDead);
    }

    private void accumulatePlayerDamage(GameData gameData, Permanent atk, CombatantStats atkStats,
                                         int damage, UUID defenderId, Permanent redirectTarget,
                                         CombatDamageState state) {
        // Check if the attacker is attacking a planeswalker instead of a player
        UUID attackTarget = atk.getAttackTarget();
        if (attackTarget != null && !gameData.playerIds.contains(attackTarget)) {
            // CR 510.1b: If the planeswalker left the battlefield, the creature assigns no combat damage
            Permanent pw = gameQueryService.findPermanentById(gameData, attackTarget);
            if (pw == null) return;
            // Attacking a planeswalker — damage removes loyalty counters (CR 306.8)
            // Apply one-shot Sanctum Guardian shields (prevent the next damage from the chosen source to any target)
            damage = damagePreventionService.applyChosenSourceNextDamageToAnyTargetShield(gameData, atk.getId(), damage);
            state.damageToPlaneswalkers.merge(attackTarget, damage, Integer::sum);
            state.combatDamageDealt.merge(atk, damage, Integer::sum);
            return;
        }

        // CR 614 — Replacement effect: if a matching creature would deal combat damage to a
        // player, instead that player mills that many cards (e.g. Undead Alchemist).
        if (damage > 0 && redirectTarget == null) {
            UUID atkControllerId = gameQueryService.findPermanentController(gameData, atk.getId());
            if (atkControllerId != null && hasReplaceCombatDamageWithMill(gameData, atkControllerId, atk)) {
                gameBroadcastService.logAndBroadcast(gameData, atk.getCard().getName()
                        + "'s " + damage + " combat damage is replaced with milling.");
                graveyardService.resolveMillPlayer(gameData, defenderId, damage);
                return;
            }
        }

        boolean atkHasInfect = atkStats.infect();
        if (redirectTarget != null) {
            // The guard is a creature, so wither also routes here (damage becomes -1/-1 counters).
            if (gameQueryService.dealsCounterDamageToCreatures(gameData, atk)) {
                state.infectDamageRedirectedToGuard += damage;
            } else {
                state.damageRedirectedToGuard += damage;
            }
        } else if (damagePreventionService.isSourceDamagePreventedForPlayer(gameData, defenderId, atk.getId())) {
            // Source-specific damage prevention — skip this damage
        } else {
            // Apply source-specific redirect shields (e.g. Harm's Way) per-attacker.
            // Redirection is a replacement effect, not prevention, so it fires before prevention checks.
            damage = damagePreventionService.applySourceRedirectShields(gameData, defenderId, atk.getId(), damage);
            processSourceRedirectDamage(gameData);
            CardColor attackerColor = atkStats.color();
            if (damage > 0
                    && !(gameQueryService.isDamagePreventable(gameData)
                            && gameQueryService.playerHasProtectionFromColor(gameData, defenderId, attackerColor))
                    && !(gameQueryService.isDamagePreventable(gameData)
                            && gameQueryService.playerHasProtectionFromChosenName(gameData, defenderId, atk.getCard().getName()))
                    && !damagePreventionService.applyColorDamagePreventionForPlayer(gameData, defenderId, attackerColor)) {
                UUID attackerControllerId = gameQueryService.findPermanentController(gameData, atk.getId());
                damage = damagePreventionService.applyOpponentSourceDamageReduction(gameData, defenderId, attackerControllerId, damage);
                // Apply target+source-specific prevention shields (e.g. Healing Grace)
                damage = damagePreventionService.applyTargetSourcePreventionShield(gameData, defenderId, atk.getId(), damage);
                // Apply one-shot Circle-of-Protection shields (prevent the next damage event from the chosen source)
                damage = damagePreventionService.applyPlayerNextSourceDamageShield(gameData, defenderId, atk.getId(), damage);
                // Apply one-shot Sanctum Guardian shields (prevent the next damage from the chosen source to any target)
                damage = damagePreventionService.applyChosenSourceNextDamageToAnyTargetShield(gameData, atk.getId(), damage);
                // Battletide Alchemist: the defending player prevents up to (Clerics they control) of this attacker's damage.
                int battletidePrevented = damagePreventionService.applyControllerPerClericDamagePrevention(gameData, defenderId, damage);
                if (battletidePrevented > 0) {
                    damage -= battletidePrevented;
                    gameBroadcastService.logAndBroadcast(gameData,
                            battletidePrevented + " of " + atk.getCard().getName() + "'s combat damage to "
                                    + gameData.playerIdToName.get(defenderId) + " is prevented.");
                }
                // Urza's Armor: the defending player prevents a fixed amount of this attacker's damage.
                int fixedPrevented = damagePreventionService.applyControllerFixedPerSourceDamagePrevention(gameData, defenderId, damage);
                if (fixedPrevented > 0) {
                    damage -= fixedPrevented;
                    gameBroadcastService.logAndBroadcast(gameData,
                            fixedPrevented + " of " + atk.getCard().getName() + "'s combat damage to "
                                    + gameData.playerIdToName.get(defenderId) + " is prevented.");
                }
                if (atkHasInfect) {
                    state.poisonDamageToDefendingPlayer += damage;
                } else {
                    state.damageToDefendingPlayer += damage;
                }
            }
        }
        state.combatDamageDealt.merge(atk, damage, Integer::sum);
        state.combatDamageDealtToPlayer.merge(atk, damage, Integer::sum);
    }

    private Map<Integer, Integer> precomputeBlockerDamage(DamagePhaseSnapshot snap,
                                                           Map<Integer, List<Integer>> blockerMap,
                                                           Set<Integer> deadDefenderIndices,
                                                           boolean requireFirstStrike) {
        Map<Integer, Integer> blockerDamage = new HashMap<>();
        for (List<Integer> blkList : blockerMap.values()) {
            for (int blkIdx : blkList) {
                CombatantStats blkStats = snap.defenderStats().get(blkIdx);
                if (requireFirstStrike) {
                    if (blkStats.firstStrike() || blkStats.doubleStrike()) {
                        blockerDamage.putIfAbsent(blkIdx, blkStats.combatDamage());
                    }
                } else {
                    if (!deadDefenderIndices.contains(blkIdx)) {
                        blockerDamage.putIfAbsent(blkIdx, blkStats.combatDamage());
                    }
                }
            }
        }
        return blockerDamage;
    }

    private void applyCombatCreatureDamage(GameData gameData, Permanent source, CombatantStats sourceStats,
                                           Permanent target, int targetIdx, int damage,
                                           Map<Integer, Integer> damageTakenMap,
                                           Set<Integer> deathtouchDamagedSet) {
        // Apply source-specific redirect shields (e.g. Harm's Way) per-source for creature targets
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (targetControllerId != null) {
            damage = damagePreventionService.applySourceRedirectShields(gameData, targetControllerId, source.getId(), damage);
            processSourceRedirectDamage(gameData);
        }
        // Apply creature-specific redirect shields (e.g. Oracle's Attendants) per-source for creature targets
        damage = damagePreventionService.applyCreatureRedirectShields(gameData, target.getId(), source.getId(), damage);
        processSourceRedirectDamage(gameData);
        // Apply target+source-specific prevention shields (e.g. Healing Grace) before generic creature prevention
        damage = damagePreventionService.applyTargetSourcePreventionShield(gameData, target.getId(), source.getId(), damage);
        // Apply one-shot Sanctum Guardian shields (prevent the next damage from the chosen source to any target)
        damage = damagePreventionService.applyChosenSourceNextDamageToAnyTargetShield(gameData, source.getId(), damage);
        // Swans of Bryn Argoll: prevent all combat damage to this creature; the source's controller draws that many cards.
        UUID swansSourceControllerId = gameQueryService.findPermanentController(gameData, source.getId());
        if (damagePreventionService.applySwansSourceControllerDraw(gameData, target, damage, swansSourceControllerId)) {
            gameBroadcastService.logAndBroadcast(gameData,
                    "Combat damage to " + target.getCard().getName() + " is prevented.");
            return;
        }
        if (gameQueryService.dealsCounterDamageToCreatures(gameData, source)) {
            int afterShield = damagePreventionService.applyCreaturePreventionShield(gameData, target, damage, true);
            if (afterShield > 0 && !gameQueryService.cantHaveCounters(gameData, target)
                    && !gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, target)) {
                target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + afterShield);
            }
        } else {
            damageTakenMap.merge(targetIdx, damage, Integer::sum);
        }
        if (damage > 0 && sourceStats.deathtouch()) {
            deathtouchDamagedSet.add(targetIdx);
        }
    }

    private void recordCombatDamageToCreature(GameData gameData, CombatDamageState state,
                                               Permanent source, UUID controllerId,
                                               Permanent target, int damage) {
        if (damage <= 0) return;
        state.combatDamageDealerControllers.putIfAbsent(source, controllerId);
        state.combatDamageDealtToCreatures.computeIfAbsent(source, ignored -> new ArrayList<>()).add(target.getId());
        state.combatDamageAmountsToCreatures
                .computeIfAbsent(source, ignored -> new HashMap<>())
                .merge(target.getId(), damage, Integer::sum);
        // Capture the damaged creature's controller while it is still alive (for reflection triggers).
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (targetControllerId != null) {
            state.combatDamageTargetControllers.putIfAbsent(target.getId(), targetControllerId);
        }
        graveyardService.recordCreatureDamagedByPermanent(gameData, source.getId(), target, damage);
        triggerCollectionService.checkEnchantedCreatureDealtDamageTriggers(gameData, target, damage);
    }


    private boolean hasFirstOrDoubleStrike(GameData gameData, Permanent creature) {
        return gameQueryService.hasKeyword(gameData, creature, Keyword.FIRST_STRIKE)
                || gameQueryService.hasKeyword(gameData, creature, Keyword.DOUBLE_STRIKE);
    }

    /**
     * Returns {@code true} if any permanent on the controller's battlefield has a
     * {@link ReplaceCombatDamageWithMillEffect} whose predicate matches the attacker.
     */
    private boolean hasReplaceCombatDamageWithMill(GameData gameData, UUID controllerId, Permanent attacker) {
        List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
        if (bf == null) return false;
        for (Permanent perm : bf) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof ReplaceCombatDamageWithMillEffect replacement
                        && predicateEvaluationService.matchesPermanentPredicate(gameData, attacker, replacement.attackerPredicate())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean assignsCombatDamageAsThoughUnblocked(Permanent attacker) {
        for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof AssignCombatDamageAsThoughUnblockedEffect) return true;
        }
        return false;
    }

    private boolean assignsUnblockedDamageToDefendingCreature(Permanent attacker) {
        for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof AssignCombatDamageToDefendingCreatureWhenUnblockedEffect) return true;
        }
        return false;
    }

    /**
     * Returns {@code true} if the unblocked attacker may redirect its combat damage to a
     * defending creature (e.g. Cunning Giant) and the defending player controls at least one
     * creature to redirect to. Only applies when attacking a player (not a planeswalker).
     */
    private boolean canRedirectUnblockedDamageToDefendingCreature(GameData gameData, Permanent atk,
                                                                  UUID defenderId, List<Permanent> defBf) {
        if (!assignsUnblockedDamageToDefendingCreature(atk)) return false;
        UUID attackTarget = atk.getAttackTarget();
        if (attackTarget != null && !gameData.playerIds.contains(attackTarget)) return false;
        if (defBf == null) return false;
        for (Permanent p : defBf) {
            if (gameQueryService.isCreature(gameData, p)) return true;
        }
        return false;
    }

    private boolean needsManualDamageAssignment(GameData gameData, Permanent atk,
                                                List<Integer> livingBlockerIndices,
                                                UUID defenderId, List<Permanent> defBf) {
        // A creature with 0 or negative power deals no combat damage (CR 510.1a),
        // so there is nothing for the player to distribute.
        if (gameQueryService.getEffectiveCombatDamage(gameData, atk) <= 0) return false;
        if (livingBlockerIndices.isEmpty()) {
            // Unblocked attacker that may assign its combat damage to a defending creature
            // (e.g. Cunning Giant). Prompt only when there is a defending creature to choose.
            return canRedirectUnblockedDamageToDefendingCreature(gameData, atk, defenderId, defBf);
        }
        if (livingBlockerIndices.size() >= 2) return true;
        if (gameQueryService.hasKeyword(gameData, atk, Keyword.TRAMPLE)) return true;
        if (assignsCombatDamageAsThoughUnblocked(atk)) return true;
        return false;
    }

    private void restorePhase1State(GameData gameData, CombatDamageState state) {
        CombatDamagePhase1State p1 = gameData.combatDamagePhase1State;
        state.deadAttackerIndices.addAll(p1.deadAttackerIndices);
        state.deadDefenderIndices.addAll(p1.deadDefenderIndices);
        state.atkDamageTaken.putAll(p1.atkDamageTaken);
        state.defDamageTaken.putAll(p1.defDamageTaken);
        state.damageToDefendingPlayer = p1.damageToDefendingPlayer;
        state.damageRedirectedToGuard = p1.damageRedirectedToGuard;
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
                dealtByUUID, dealtToPlayerByUUID, dealtToCreaturesByUUID, dealerControllersByUUID,
                damageAmountsByUUID,
                state.damageToDefendingPlayer, state.damageRedirectedToGuard,
                new HashMap<>(state.damageToPlaneswalkers),
                new LinkedHashMap<>(blockerMap), anyFirstStrike,
                new HashSet<>(state.deathtouchDamagedAttackerIndices), new HashSet<>(state.deathtouchDamagedDefenderIndices));
    }

    private void sendNextCombatDamageAssignment(GameData gameData, List<Permanent> atkBf,
                                                 List<Permanent> defBf, UUID activeId, UUID defenderId) {
        int atkIdx = gameData.combatDamagePendingIndices.get(0);
        Permanent atk = atkBf.get(atkIdx);
        CombatDamagePhase1State p1 = gameData.combatDamagePhase1State;
        List<Integer> blkIndices = p1.blockerMap.get(atkIdx);
        List<Integer> livingBlockers = new ArrayList<>();
        for (int i : blkIndices) {
            if (!p1.deadDefenderIndices.contains(i)) livingBlockers.add(i);
        }

        List<CombatDamageTarget> domainTargets = new ArrayList<>();
        for (int blkIdx : livingBlockers) {
            Permanent blk = defBf.get(blkIdx);
            int toughness = gameQueryService.getEffectiveToughness(gameData, blk);
            int damageTaken = p1.defDamageTaken.getOrDefault(blkIdx, 0);
            domainTargets.add(new CombatDamageTarget(
                    blk.getId(), blk.getCard().getName(), toughness, damageTaken, false));
        }

        boolean isTrample = gameQueryService.hasKeyword(gameData, atk, Keyword.TRAMPLE);
        boolean isDeathtouch = gameQueryService.hasKeyword(gameData, atk, Keyword.DEATHTOUCH);
        // Unblocked attacker that may redirect its whole combat damage to one defending creature
        // (e.g. Cunning Giant): offer every defending creature plus the defending player.
        boolean unblockedRedirect = livingBlockers.isEmpty()
                && canRedirectUnblockedDamageToDefendingCreature(gameData, atk, defenderId, defBf);
        if (unblockedRedirect) {
            for (Permanent def : defBf) {
                if (gameQueryService.isCreature(gameData, def)) {
                    domainTargets.add(new CombatDamageTarget(def.getId(), def.getCard().getName(),
                            gameQueryService.getEffectiveToughness(gameData, def), def.getMarkedDamage(), false));
                }
            }
        }
        boolean addOverflow = isTrample || assignsCombatDamageAsThoughUnblocked(atk) || unblockedRedirect;
        if (addOverflow) {
            UUID overflowTarget = atk.getAttackTarget() != null ? atk.getAttackTarget() : defenderId;
            if (gameData.playerIds.contains(overflowTarget)) {
                String defenderName = gameData.playerIdToName.get(overflowTarget);
                domainTargets.add(new CombatDamageTarget(
                        overflowTarget, defenderName, 0, 0, true));
            } else {
                Permanent pw = gameQueryService.findPermanentById(gameData, overflowTarget);
                if (pw != null) {
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

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.CombatDamageAssignment(
                activeId, atkIdx, atk.getId(), atk.getCard().getName(), totalDamage,
                domainTargets, isTrample, isDeathtouch, unblockedRedirect));
    }
}
