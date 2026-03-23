package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
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
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageAsThoughUnblockedEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawAndDiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGetsPoisonCounterEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerByHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsRepeatOnDuplicateEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToDamageDealtEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnDamageDealerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceCombatDamageWithMillEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentsOnCombatDamageToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.SphinxAmbassadorEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardOrControllerDrawsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.CombatDamageAssignmentNotification;
import com.github.laxika.magicalvibes.networking.model.CombatDamageTargetView;
import com.github.laxika.magicalvibes.service.effect.LifeResolutionService;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.DeathTriggerService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;
    private final DamagePreventionService damagePreventionService;
    private final GraveyardService graveyardService;
    private final DeathTriggerService deathTriggerService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final SessionManager sessionManager;
    private final TriggerCollectionService triggerCollectionService;
    private final LifeResolutionService lifeResolutionService;
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

        // Build blocker map: attackerIndex -> list of blockerIndices
        Map<Integer, List<Integer>> blockerMap = new LinkedHashMap<>();
        for (int atkIdx : attackingIndices) {
            List<Integer> blockers = new ArrayList<>();
            for (int i = 0; i < defBf.size(); i++) {
                if (defBf.get(i).isBlocking() && defBf.get(i).getBlockingTargets().contains(atkIdx)) {
                    blockers.add(i);
                }
            }
            blockerMap.put(atkIdx, blockers);
        }

        // Check if any combat creature has first strike or double strike
        boolean anyFirstStrike = attackingIndices.stream()
                        .anyMatch(i -> hasFirstOrDoubleStrike(gameData, atkBf.get(i)))
                || blockerMap.values().stream().flatMapToInt(l -> l.stream().mapToInt(i -> i))
                        .anyMatch(i -> hasFirstOrDoubleStrike(gameData, defBf.get(i)));

        CombatDamageState state = new CombatDamageState();

        // Restore phase 1 state if re-entering after damage assignment
        if (gameData.combatDamagePhase1Complete) {
            restorePhase1State(gameData, state);
        }

        // Phase 1: First strike damage (skip on re-entry)
        if (!gameData.combatDamagePhase1Complete && anyFirstStrike) {
            resolveDamagePhase(gameData, state, blockerMap, atkBf, defBf,
                    attackingIndices, activeId, defenderId, redirectTarget, true);
        }

        // Check if any phase 2 attackers need manual damage assignment
        if (!gameData.combatDamagePhase1Complete) {
            List<Integer> needsManual = new ArrayList<>();
            for (var bEntry : blockerMap.entrySet()) {
                int bAtkIdx = bEntry.getKey();
                if (state.deadAttackerIndices.contains(bAtkIdx)) continue;
                Permanent bAtk = atkBf.get(bAtkIdx);
                boolean bAtkSkipPhase2 = gameQueryService.hasKeyword(gameData, bAtk, Keyword.FIRST_STRIKE)
                        && !gameQueryService.hasKeyword(gameData, bAtk, Keyword.DOUBLE_STRIKE);
                if (bAtkSkipPhase2) continue;
                if (gameQueryService.isPreventedFromDealingDamage(gameData, bAtk, true)) continue;
                List<Integer> livingBlockers = bEntry.getValue().stream()
                        .filter(i -> !state.deadDefenderIndices.contains(i))
                        .toList();
                if (needsManualDamageAssignment(gameData, bAtk, livingBlockers)) {
                    needsManual.add(bAtkIdx);
                }
            }
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

        resolveRedirectedDamage(gameData, state, redirectTarget);

        // Process lifelink before removing dead creatures
        processLifelink(gameData, state.combatDamageDealt);
        processGainLifeEqualToDamageDealt(gameData, state.combatDamageDealt);

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

        // Process ON_DEALT_DAMAGE triggers (e.g. Nested Ghoul)
        processDealtDamageTriggers(gameData, dealtDamageTriggerData);

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
        int totalAssigned = assignments.values().stream().mapToInt(Integer::intValue).sum();
        if (totalAssigned != totalDamage) {
            throw new IllegalStateException("Total assigned damage (" + totalAssigned
                    + ") must equal attacker's combat damage (" + totalDamage + ")");
        }

        // Validate targets
        CombatDamagePhase1State p1 = gameData.combatDamagePhase1State;
        List<Integer> blkIndices = p1.blockerMap.get(attackerIndex);
        List<Integer> livingBlockers = blkIndices.stream()
                .filter(i -> !p1.deadDefenderIndices.contains(i))
                .toList();

        // Determine the overflow target: the player or planeswalker being attacked
        UUID overflowTargetId = atk.getAttackTarget() != null ? atk.getAttackTarget() : defenderId;

        Set<UUID> validTargetIds = new HashSet<>();
        for (int blkIdx : livingBlockers) {
            validTargetIds.add(defBf.get(blkIdx).getId());
        }
        boolean canTargetOverflow = gameQueryService.hasKeyword(gameData, atk, Keyword.TRAMPLE)
                || assignsCombatDamageAsThoughUnblocked(atk);
        if (canTargetOverflow) {
            validTargetIds.add(overflowTargetId);
        }

        for (UUID targetId : assignments.keySet()) {
            if (!validTargetIds.contains(targetId)) {
                throw new IllegalStateException("Invalid damage target: " + targetId);
            }
        }

        // Validate trample: each blocker must receive at least lethal damage
        if (gameQueryService.hasKeyword(gameData, atk, Keyword.TRAMPLE)) {
            boolean atkHasDeathtouchForValidation = gameQueryService.hasKeyword(gameData, atk, Keyword.DEATHTOUCH);

            // Compute total lethal needed across all blockers
            int totalLethalNeeded = 0;
            for (int blkIdx : livingBlockers) {
                Permanent blk = defBf.get(blkIdx);
                int alreadyTaken = p1.defDamageTaken.getOrDefault(blkIdx, 0);
                int lethal = atkHasDeathtouchForValidation
                        ? Math.max(0, 1 - alreadyTaken)
                        : Math.max(0, gameQueryService.getEffectiveToughness(gameData, blk) - alreadyTaken);
                totalLethalNeeded += lethal;
            }

            if (totalDamage >= totalLethalNeeded) {
                // Enough damage to deal lethal to all blockers: enforce per-blocker minimums
                for (int blkIdx : livingBlockers) {
                    Permanent blk = defBf.get(blkIdx);
                    int alreadyTaken = p1.defDamageTaken.getOrDefault(blkIdx, 0);
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
                Permanent target = defBf.stream().filter(p -> p.getId().equals(entry.getKey())).findFirst().orElse(null);
                parts.add(entry.getValue() + " to " + (target != null ? target.getCard().getName() : entry.getKey()));
            }
        }
        log.info("Game {} - Combat damage assigned for [{}]: {} -> {}", gameData.id, attackerIndex,
                atk.getCard().getName(), String.join(", ", parts));
    }

    // ===== Damage phase resolution =====

    private void resolveDamagePhase(GameData gameData, CombatDamageState state,
                                     Map<Integer, List<Integer>> blockerMap,
                                     List<Permanent> atkBf, List<Permanent> defBf,
                                     List<Integer> attackingIndices,
                                     UUID activeId, UUID defenderId,
                                     Permanent redirectTarget, boolean isFirstStrikePhase) {
        Map<Integer, Integer> blockerDamage = precomputeBlockerDamage(
                gameData, blockerMap, defBf, state.deadDefenderIndices, isFirstStrikePhase);

        // Track remaining damage for multi-blocking creatures (CR 510.1c-d)
        Map<Integer, Integer> blockerRemainingDamage = new HashMap<>(blockerDamage);

        for (var entry : blockerMap.entrySet()) {
            int atkIdx = entry.getKey();
            List<Integer> blkIndices = entry.getValue();

            if (!isFirstStrikePhase && state.deadAttackerIndices.contains(atkIdx)) continue;

            Permanent atk = atkBf.get(atkIdx);
            boolean atkParticipates = participatesInDamagePhase(gameData, atk, isFirstStrikePhase);

            Map<UUID, Integer> playerAssignment = isFirstStrikePhase ? null
                    : gameData.combatDamagePlayerAssignments.get(atkIdx);

            boolean assignAsUnblocked = !blkIndices.isEmpty() && assignsCombatDamageAsThoughUnblocked(atk);

            if (playerAssignment != null) {
                if (atkParticipates && !gameQueryService.isPreventedFromDealingDamage(gameData, atk, true)) {
                    applyPlayerAssignedDamage(gameData, state, atk, blkIndices, defBf,
                            playerAssignment, activeId, defenderId, redirectTarget);
                }
            } else if (blkIndices.isEmpty() || assignAsUnblocked) {
                if (atkParticipates && !gameQueryService.isPreventedFromDealingDamage(gameData, atk, true)) {
                    int power = gameQueryService.applyCombatDamageMultiplier(gameData, gameQueryService.getEffectiveCombatDamage(gameData, atk), atk, null);
                    accumulatePlayerDamage(gameData, atk, power, defenderId, redirectTarget, state);
                }
            } else {
                if (atkParticipates && !gameQueryService.isPreventedFromDealingDamage(gameData, atk, true)) {
                    distributeAttackerDamageToBlockers(gameData, state, atk, blkIndices, defBf,
                            activeId, defenderId, redirectTarget, !isFirstStrikePhase);
                }
            }

            if (!blkIndices.isEmpty()) {
                for (int blkIdx : blkIndices) {
                    if (!isFirstStrikePhase && state.deadDefenderIndices.contains(blkIdx)) continue;
                    Permanent blk = defBf.get(blkIdx);
                    boolean blkParticipates = participatesInDamagePhase(gameData, blk, isFirstStrikePhase);
                    if (blkParticipates && !gameQueryService.isPreventedFromDealingDamage(gameData, blk, true)
                            && !(gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, atk, blk))) {
                        // For multi-blocking creatures, distribute damage per CR 510.1c-d:
                        // assign lethal to each attacker in order before moving on
                        int blkTargetCount = blk.getBlockingTargets().size();
                        int assignedDmg;
                        if (blkTargetCount <= 1) {
                            assignedDmg = blockerDamage.getOrDefault(blkIdx, 0);
                        } else {
                            int blkRemaining = blockerRemainingDamage.getOrDefault(blkIdx, 0);
                            boolean blkHasDeathtouch = gameQueryService.hasKeyword(gameData, blk, Keyword.DEATHTOUCH);
                            int atkToughness = gameQueryService.getEffectiveToughness(gameData, atk);
                            int atkDamageSoFar = state.atkDamageTaken.getOrDefault(atkIdx, 0);
                            int lethalNeeded = blkHasDeathtouch
                                    ? Math.max(0, 1 - atkDamageSoFar)
                                    : Math.max(0, atkToughness - atkDamageSoFar);
                            assignedDmg = Math.min(blkRemaining, lethalNeeded);
                            blockerRemainingDamage.put(blkIdx, blkRemaining - assignedDmg);
                        }
                        int actualDmg = gameQueryService.applyCombatDamageMultiplier(gameData, assignedDmg, blk, atk);
                        applyCombatCreatureDamage(gameData, blk, atk, atkIdx, actualDmg, state.atkDamageTaken, state.deathtouchDamagedAttackerIndices);
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

    private void distributeAttackerDamageToBlockers(GameData gameData, CombatDamageState state,
                                                     Permanent atk, List<Integer> blkIndices,
                                                     List<Permanent> defBf,
                                                     UUID activeId, UUID defenderId,
                                                     Permanent redirectTarget,
                                                     boolean skipDeadBlockers) {
        boolean atkHasDeathtouch = gameQueryService.hasKeyword(gameData, atk, Keyword.DEATHTOUCH);
        int remaining = gameQueryService.getEffectiveCombatDamage(gameData, atk);
        for (int blkIdx : blkIndices) {
            if (skipDeadBlockers && state.deadDefenderIndices.contains(blkIdx)) continue;
            Permanent blk = defBf.get(blkIdx);
            int lethalNeeded = atkHasDeathtouch
                    ? Math.max(0, 1 - state.defDamageTaken.getOrDefault(blkIdx, 0))
                    : gameQueryService.getEffectiveToughness(gameData, blk) - state.defDamageTaken.getOrDefault(blkIdx, 0);
            int dmg = Math.min(remaining, Math.max(0, lethalNeeded));
            if (!(gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, blk, atk))) {
                int actualDmg = gameQueryService.applyCombatDamageMultiplier(gameData, dmg, atk, blk);
                applyCombatCreatureDamage(gameData, atk, blk, blkIdx, actualDmg, state.defDamageTaken, state.deathtouchDamagedDefenderIndices);
                state.combatDamageDealt.merge(atk, actualDmg, Integer::sum);
                recordCombatDamageToCreature(gameData, state, atk, activeId, blk, actualDmg);
            }
            remaining -= dmg;
        }
        if (remaining > 0 && gameQueryService.hasKeyword(gameData, atk, Keyword.TRAMPLE)) {
            int doubledRemaining = gameQueryService.applyCombatDamageMultiplier(gameData, remaining, atk, null);
            accumulatePlayerDamage(gameData, atk, doubledRemaining, defenderId, redirectTarget, state);
        }
    }

    private void applyPlayerAssignedDamage(GameData gameData, CombatDamageState state,
                                            Permanent atk, List<Integer> blkIndices,
                                            List<Permanent> defBf,
                                            Map<UUID, Integer> playerAssignment,
                                            UUID activeId, UUID defenderId,
                                            Permanent redirectTarget) {
        UUID overflowTargetId = atk.getAttackTarget() != null ? atk.getAttackTarget() : defenderId;
        for (var dmgEntry : playerAssignment.entrySet()) {
            UUID targetId = dmgEntry.getKey();
            int dmg = dmgEntry.getValue();
            if (dmg <= 0) continue;
            if (targetId.equals(overflowTargetId)) {
                // Overflow damage to the attack target (player or planeswalker)
                int actualDmg = gameQueryService.applyCombatDamageMultiplier(gameData, dmg, atk, null);
                accumulatePlayerDamage(gameData, atk, actualDmg, defenderId, redirectTarget, state);
            } else {
                for (int blkIdx : blkIndices) {
                    Permanent blk = defBf.get(blkIdx);
                    if (blk.getId().equals(targetId)) {
                        if (!(gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, blk, atk))) {
                            int actualDmg = gameQueryService.applyCombatDamageMultiplier(gameData, dmg, atk, blk);
                            applyCombatCreatureDamage(gameData, atk, blk, blkIdx, actualDmg, state.defDamageTaken, state.deathtouchDamagedDefenderIndices);
                            state.combatDamageDealt.merge(atk, actualDmg, Integer::sum);
                            recordCombatDamageToCreature(gameData, state, atk, activeId, blk, actualDmg);
                        }
                        break;
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
                redirectTarget.setMinusOneMinusOneCounters(redirectTarget.getMinusOneMinusOneCounters() + state.infectDamageRedirectedToGuard);
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

    // ===== Lifelink and life gain =====

    private void processLifelink(GameData gameData, Map<Permanent, Integer> combatDamageDealt) {
        for (var entry : combatDamageDealt.entrySet()) {
            Permanent creature = entry.getKey();
            int damageDealt = entry.getValue();
            if (damageDealt <= 0) continue;
            if (!gameQueryService.hasKeyword(gameData, creature, Keyword.LIFELINK)) continue;
            UUID controllerId = CombatHelper.findControllerOf(gameData, creature);
            if (controllerId == null) continue;
            lifeResolutionService.applyGainLife(gameData, controllerId, damageDealt, "lifelink");
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
                            lifeResolutionService.applyGainLife(gameData, playerId, damageDealt, perm.getCard().getName());
                        }
                    }
                }
            });
        }
    }

    // ===== Combat damage triggers =====

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

            List<CardEffect> allDamageEffects = new ArrayList<>();
            allDamageEffects.addAll(creature.getCard().getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER));
            allDamageEffects.addAll(creature.getCard().getEffects(EffectSlot.ON_DAMAGE_TO_PLAYER));
            for (CardEffect effect : allDamageEffects) {
                if (effect instanceof MetalcraftConditionalEffect metalcraft) {
                    if (!gameQueryService.isMetalcraftMet(gameData, attackerId)) {
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
                    if (may.wrapped() instanceof SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect) {
                        List<Permanent> defenderBf = gameData.playerBattlefields.get(defenderId);
                        boolean hasCreatureTargets = defenderBf != null && defenderBf.stream()
                                .anyMatch(p -> gameQueryService.isCreature(gameData, p));
                        if (!hasCreatureTargets) {
                            gameBroadcastService.logAndBroadcast(gameData, creature.getCard().getName()
                                    + "'s ability does not trigger — " + gameData.playerIdToName.get(defenderId) + " has no creatures.");
                            continue;
                        }
                    }
                    if (may.wrapped() instanceof ExilePermanentDamagedPlayerControlsEffect exileEffect) {
                        List<Permanent> defenderBf = gameData.playerBattlefields.get(defenderId);
                        boolean hasValidTargets = defenderBf != null && defenderBf.stream()
                                .anyMatch(p -> exileEffect.predicate() == null
                                        || gameQueryService.matchesPermanentPredicate(gameData, p, exileEffect.predicate()));
                        if (!hasValidTargets) {
                            gameBroadcastService.logAndBroadcast(gameData, creature.getCard().getName()
                                    + "'s ability does not trigger — " + gameData.playerIdToName.get(defenderId) + " has no valid targets.");
                            continue;
                        }
                    }
                    gameData.queueMayAbility(creature.getCard(), attackerId, may, defenderId, creature.getId());
                    gameBroadcastService.logAndBroadcast(gameData, creature.getCard().getName() + "'s combat damage trigger fires.");
                    continue;
                }

                String desc = creature.getCard().getName() + "'s triggered ability";
                StackEntry se;
                if (effect instanceof ReturnPermanentsOnCombatDamageToPlayerEffect
                        || effect instanceof DealDamageToEachCreatureDamagedPlayerControlsEffect) {
                    se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            desc, List.of(effect), damageDealt, defenderId, null);
                } else if (effect instanceof PutCountersOnSourceEffect
                        || effect instanceof ExploreEffect) {
                    se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            desc, List.of(effect), null, creature.getId());
                } else if (effect instanceof ExileTopCardsRepeatOnDuplicateEffect
                        || effect instanceof TargetPlayerDiscardsEffect
                        || effect instanceof TargetPlayerRandomDiscardEffect
                        || effect instanceof TargetPlayerRandomDiscardOrControllerDrawsEffect
                        || effect instanceof RevealRandomCardFromTargetPlayerHandEffect
                        || effect instanceof SphinxAmbassadorEffect
                        || effect instanceof MillTargetPlayerEffect
                        || effect instanceof DealDamageToTargetPlayerByHandSizeEffect) {
                    se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            desc, List.of(effect), defenderId, creature.getId());
                } else {
                    se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), attackerId,
                            desc, List.of(effect));
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
            checkAllyCreatureCombatDamageToPlayerTriggers(gameData, creature, attackerId);
        }
    }

    private void checkAttachedCombatDamageToPlayerTriggers(GameData gameData, Permanent creature, UUID attackerId, UUID defenderId) {
        gameData.forEachPermanent((ownerId, perm) -> {
            if (perm.isAttached() && perm.getAttachedTo().equals(creature.getId())) {
                List<CardEffect> effects = new ArrayList<>();
                effects.addAll(perm.getCard().getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER));
                effects.addAll(perm.getCard().getEffects(EffectSlot.ON_DAMAGE_TO_PLAYER));
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
    private void checkAllyCreatureCombatDamageToPlayerTriggers(GameData gameData, Permanent creature, UUID attackerId) {
        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(attackerId);
        if (attackerBattlefield == null) return;

        for (Permanent perm : attackerBattlefield) {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER);
            for (CardEffect effect : effects) {
                if (effect instanceof PutCountersOnDamageDealerEffect dealerEffect) {
                    if (dealerEffect.predicate() != null
                            && !gameQueryService.matchesPermanentPredicate(gameData, creature, dealerEffect.predicate())) {
                        continue;
                    }
                    StackEntry se = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            attackerId,
                            perm.getCard().getName() + "'s triggered ability",
                            List.of(new PutCountersOnSourceEffect(dealerEffect.powerModifier(), dealerEffect.toughnessModifier(), dealerEffect.amount())),
                            null,
                            creature.getId()
                    );
                    se.setNonTargeting(true);
                    gameData.stack.add(se);
                    gameBroadcastService.logAndBroadcast(gameData, perm.getCard().getName()
                            + "'s triggered ability goes on the stack.");
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
        if (gameData.pendingDelayedCombatDamageLoots.isEmpty()) return;

        // Check if any creature controlled by the trigger's controller dealt combat damage to a player
        for (GameData.DelayedCombatDamageLoot loot : gameData.pendingDelayedCombatDamageLoots) {
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

    // ===== Creature death and player damage =====

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
        state.damageToDefendingPlayer = damagePreventionService.applyPlayerPreventionShield(gameData, defenderId, state.damageToDefendingPlayer);
        processPendingRedirectDamage(gameData);
        state.damageToDefendingPlayer = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, defenderId, state.damageToDefendingPlayer, "combat", true);
        // Phyrexian Unlife: convert normal combat damage to poison when at 0 or less life
        if (state.damageToDefendingPlayer > 0 && gameQueryService.shouldDamageBeDealtAsInfect(gameData, defenderId)) {
            state.poisonDamageToDefendingPlayer += state.damageToDefendingPlayer;
            state.damageToDefendingPlayer = 0;
        }
        if (state.damageToDefendingPlayer > 0) {
            if (gameQueryService.canPlayerLifeChange(gameData, defenderId)) {
                int currentLife = gameData.getLife(defenderId);
                gameData.playerLifeTotals.put(defenderId, currentLife - state.damageToDefendingPlayer);
                String logEntry = gameData.playerIdToName.get(defenderId) + " takes " + state.damageToDefendingPlayer + " combat damage.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                triggerCollectionService.checkLifeLossTriggers(gameData, defenderId, state.damageToDefendingPlayer);
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
            gameData.playersDealtDamageThisTurn.add(defenderId);
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
                gameData.playersDealtDamageThisTurn.add(targetId);
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
            pw.setLoyaltyCounters(pw.getLoyaltyCounters() - damage);
            String logEntry = pw.getCard().getName() + " takes " + damage + " combat damage ("
                    + pw.getLoyaltyCounters() + " loyalty remaining).";
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
                    gameData.playersDealtDamageThisTurn.add(targetId);
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

    // ===== Damage computation helpers =====

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
        determineCasualties(gameData, new ArrayList<>(seen), defBf, state.defDamageTaken,
                state.deathtouchDamagedDefenderIndices, state.deadDefenderIndices, skipAlreadyDead);
    }

    private void accumulatePlayerDamage(GameData gameData, Permanent atk, int damage,
                                         UUID defenderId, Permanent redirectTarget,
                                         CombatDamageState state) {
        // Check if the attacker is attacking a planeswalker instead of a player
        UUID attackTarget = atk.getAttackTarget();
        if (attackTarget != null && !gameData.playerIds.contains(attackTarget)) {
            // CR 510.1b: If the planeswalker left the battlefield, the creature assigns no combat damage
            Permanent pw = gameQueryService.findPermanentById(gameData, attackTarget);
            if (pw == null) return;
            // Attacking a planeswalker — damage removes loyalty counters (CR 306.8)
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

        boolean atkHasInfect = gameQueryService.hasKeyword(gameData, atk, Keyword.INFECT);
        if (redirectTarget != null) {
            if (atkHasInfect) {
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
            if (damage > 0 && !damagePreventionService.applyColorDamagePreventionForPlayer(gameData, defenderId, atk.getEffectiveColor())) {
                UUID attackerControllerId = gameQueryService.findPermanentController(gameData, atk.getId());
                damage = damagePreventionService.applyOpponentSourceDamageReduction(gameData, defenderId, attackerControllerId, damage);
                // Apply target+source-specific prevention shields (e.g. Healing Grace)
                damage = damagePreventionService.applyTargetSourcePreventionShield(gameData, defenderId, atk.getId(), damage);
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

    private Map<Integer, Integer> precomputeBlockerDamage(GameData gameData,
                                                           Map<Integer, List<Integer>> blockerMap,
                                                           List<Permanent> defBf,
                                                           Set<Integer> deadDefenderIndices,
                                                           boolean requireFirstStrike) {
        Map<Integer, Integer> blockerDamage = new HashMap<>();
        for (List<Integer> blkList : blockerMap.values()) {
            for (int blkIdx : blkList) {
                Permanent blk = defBf.get(blkIdx);
                if (requireFirstStrike) {
                    if (hasFirstOrDoubleStrike(gameData, blk)) {
                        blockerDamage.putIfAbsent(blkIdx,
                                gameQueryService.getEffectiveCombatDamage(gameData, blk));
                    }
                } else {
                    if (!deadDefenderIndices.contains(blkIdx)) {
                        blockerDamage.putIfAbsent(blkIdx,
                                gameQueryService.getEffectiveCombatDamage(gameData, blk));
                    }
                }
            }
        }
        return blockerDamage;
    }

    private void applyCombatCreatureDamage(GameData gameData, Permanent source, Permanent target,
                                           int targetIdx, int damage, Map<Integer, Integer> damageTakenMap,
                                           Set<Integer> deathtouchDamagedSet) {
        // Apply source-specific redirect shields (e.g. Harm's Way) per-source for creature targets
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (targetControllerId != null) {
            damage = damagePreventionService.applySourceRedirectShields(gameData, targetControllerId, source.getId(), damage);
            processSourceRedirectDamage(gameData);
        }
        // Apply target+source-specific prevention shields (e.g. Healing Grace) before generic creature prevention
        damage = damagePreventionService.applyTargetSourcePreventionShield(gameData, target.getId(), source.getId(), damage);
        if (gameQueryService.hasKeyword(gameData, source, Keyword.INFECT)) {
            int afterShield = damagePreventionService.applyCreaturePreventionShield(gameData, target, damage, true);
            if (afterShield > 0 && !gameQueryService.cantHaveCounters(gameData, target)
                    && !gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, target)) {
                target.setMinusOneMinusOneCounters(target.getMinusOneMinusOneCounters() + afterShield);
            }
        } else {
            damageTakenMap.merge(targetIdx, damage, Integer::sum);
        }
        if (damage > 0 && gameQueryService.hasKeyword(gameData, source, Keyword.DEATHTOUCH)) {
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
        graveyardService.recordCreatureDamagedByPermanent(gameData, source.getId(), target, damage);
    }

    // ===== Phase state helpers =====

    private boolean hasFirstOrDoubleStrike(GameData gameData, Permanent creature) {
        return gameQueryService.hasKeyword(gameData, creature, Keyword.FIRST_STRIKE)
                || gameQueryService.hasKeyword(gameData, creature, Keyword.DOUBLE_STRIKE);
    }

    private boolean participatesInDamagePhase(GameData gameData, Permanent creature, boolean isFirstStrikePhase) {
        boolean hasFirstStrike = gameQueryService.hasKeyword(gameData, creature, Keyword.FIRST_STRIKE);
        boolean hasDoubleStrike = gameQueryService.hasKeyword(gameData, creature, Keyword.DOUBLE_STRIKE);
        return isFirstStrikePhase ? (hasFirstStrike || hasDoubleStrike) : (!hasFirstStrike || hasDoubleStrike);
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
                        && gameQueryService.matchesPermanentPredicate(gameData, attacker, replacement.attackerPredicate())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean assignsCombatDamageAsThoughUnblocked(Permanent attacker) {
        return attacker.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(AssignCombatDamageAsThoughUnblockedEffect.class::isInstance);
    }

    private boolean needsManualDamageAssignment(GameData gameData, Permanent atk, List<Integer> livingBlockerIndices) {
        if (livingBlockerIndices.isEmpty()) return false;
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
        List<Integer> livingBlockers = blkIndices.stream()
                .filter(i -> !p1.deadDefenderIndices.contains(i))
                .toList();

        List<CombatDamageTargetView> targetViews = new ArrayList<>();
        List<CombatDamageTarget> domainTargets = new ArrayList<>();
        for (int blkIdx : livingBlockers) {
            Permanent blk = defBf.get(blkIdx);
            int toughness = gameQueryService.getEffectiveToughness(gameData, blk);
            int damageTaken = p1.defDamageTaken.getOrDefault(blkIdx, 0);
            targetViews.add(new CombatDamageTargetView(
                    blk.getId().toString(), blk.getCard().getName(), toughness, damageTaken, false));
            domainTargets.add(new CombatDamageTarget(
                    blk.getId(), blk.getCard().getName(), toughness, damageTaken, false));
        }

        boolean isTrample = gameQueryService.hasKeyword(gameData, atk, Keyword.TRAMPLE);
        boolean isDeathtouch = gameQueryService.hasKeyword(gameData, atk, Keyword.DEATHTOUCH);
        boolean addOverflow = isTrample || assignsCombatDamageAsThoughUnblocked(atk);
        if (addOverflow) {
            UUID overflowTarget = atk.getAttackTarget() != null ? atk.getAttackTarget() : defenderId;
            if (gameData.playerIds.contains(overflowTarget)) {
                String defenderName = gameData.playerIdToName.get(overflowTarget);
                targetViews.add(new CombatDamageTargetView(
                        overflowTarget.toString(), defenderName, 0, 0, true));
                domainTargets.add(new CombatDamageTarget(
                        overflowTarget, defenderName, 0, 0, true));
            } else {
                Permanent pw = gameQueryService.findPermanentById(gameData, overflowTarget);
                if (pw != null) {
                    targetViews.add(new CombatDamageTargetView(
                            overflowTarget.toString(), pw.getCard().getName(), 0, 0, true));
                    domainTargets.add(new CombatDamageTarget(
                            overflowTarget, pw.getCard().getName(), 0, 0, true));
                }
            }
        }

        int totalDamage = gameQueryService.getEffectiveCombatDamage(gameData, atk);

        log.info("Game {} - Requesting combat damage assignment for [{}]: {} (damage={}, trample={}, deathtouch={}, blockers={})",
                gameData.id, atkIdx, atk.getCard().getName(), totalDamage, isTrample, isDeathtouch,
                livingBlockers.stream().map(i -> defBf.get(i).getCard().getName()
                        + " " + gameQueryService.getEffectiveToughness(gameData, defBf.get(i)) + " toughness").toList());

        gameData.interaction.beginCombatDamageAssignment(activeId, atkIdx, atk.getId(),
                atk.getCard().getName(), totalDamage, domainTargets, isTrample, isDeathtouch);

        CombatDamageAssignmentNotification notification = new CombatDamageAssignmentNotification(
                atkIdx, atk.getId().toString(), atk.getCard().getName(), totalDamage, targetViews, isTrample, isDeathtouch);
        sessionManager.sendToPlayer(CombatHelper.getEffectiveRecipient(gameData, activeId), notification);
    }
}
