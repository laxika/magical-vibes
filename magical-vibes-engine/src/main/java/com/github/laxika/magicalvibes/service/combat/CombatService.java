package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.model.action.DelayedBlockerDeclarationControl;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CombatAttackTarget;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.combat.attack.AttackLegalityService;
import com.github.laxika.magicalvibes.service.combat.attack.CombatAttackService;
import com.github.laxika.magicalvibes.service.combat.block.CombatBlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Facade for the complete combat phase lifecycle. Delegates to focused sub-services:
 * <ul>
 *   <li>{@link CombatAttackService} — attacker declaration and validation</li>
 *   <li>{@link AttackLegalityService} — which creatures may attack, and which targets</li>
 *   <li>{@link CombatBlockService} — blocker declaration and validation</li>
 *   <li>{@link CombatDamageService} — damage calculation, assignment, and triggers</li>
 *   <li>{@link CombatTriggerService} — shared trigger helpers (aura triggers, APNAP ordering)</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CombatService {

    private static final RemoveCounterFromSourceEffect REMOVE_PARALYZATION_COUNTER_EFFECT =
            new RemoveCounterFromSourceEffect(CounterType.PARALYZATION, 1);

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService triggeredAbilityStackService;
    private final CombatAttackService combatAttackService;
    private final AttackLegalityService attackLegalityService;
    private final CombatBlockService combatBlockService;
    private final CombatDamageService combatDamageService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;


    public List<Integer> getAttackableCreatureIndices(GameData gameData, UUID playerId) {
        return combatAttackService.getAttackableCreatureIndices(gameData, playerId);
    }

    public List<Integer> getMustAttackIndices(GameData gameData, UUID playerId, List<Integer> attackableIndices) {
        return combatAttackService.getMustAttackIndices(gameData, playerId, attackableIndices);
    }

    public List<CombatAttackTarget> buildAvailableTargets(GameData gameData, UUID activePlayerId) {
        return attackLegalityService.buildAvailableTargets(gameData, activePlayerId);
    }

    public boolean isOpponentForcedToAttack(GameData gameData, UUID playerId) {
        return combatAttackService.isOpponentForcedToAttack(gameData, playerId);
    }

    public void handleDeclareAttackersStep(GameData gameData) {
        combatAttackService.handleDeclareAttackersStep(gameData);
    }

    public CombatResult declareAttackers(GameData gameData, Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        return declareAttackers(gameData, player, attackerIndices, attackTargets, null);
    }

    public CombatResult declareAttackers(GameData gameData, Player player, List<Integer> attackerIndices,
                                         Map<Integer, UUID> attackTargets, List<List<Integer>> bands) {
        return combatAttackService.declareAttackers(gameData, player, attackerIndices, attackTargets, bands);
    }

    public List<Integer> getAttackingCreatureIndices(GameData gameData, UUID playerId) {
        return combatAttackService.getAttackingCreatureIndices(gameData, playerId);
    }


    public List<Integer> getBlockableCreatureIndices(GameData gameData, UUID playerId) {
        return combatBlockService.getBlockableCreatureIndices(gameData, playerId);
    }

    public List<Integer> getBlockableAttackerIndices(GameData gameData, UUID activeId, UUID defenderId) {
        return combatBlockService.getBlockableAttackerIndices(gameData, activeId, defenderId);
    }

    public Map<Integer, List<Integer>> computeLegalBlockPairs(GameData gameData,
                                                              List<Integer> blockerIndices,
                                                              List<Integer> attackerIndices,
                                                              UUID defenderId,
                                                              UUID attackerId) {
        return combatBlockService.computeLegalBlockPairs(gameData, blockerIndices, attackerIndices, defenderId, attackerId);
    }

    public CombatResult handleDeclareBlockersStep(GameData gameData) {
        return combatBlockService.handleDeclareBlockersStep(gameData);
    }

    public CombatResult declareBlockers(GameData gameData, Player player, List<BlockerAssignment> blockerAssignments) {
        return combatBlockService.declareBlockers(gameData, player, blockerAssignments);
    }


    public CombatResult resolveCombatDamage(GameData gameData) {
        return combatDamageService.resolveCombatDamage(gameData);
    }

    public void handleCombatDamageAssigned(GameData gameData, Player player, int attackerIndex, Map<UUID, Integer> assignments) {
        combatDamageService.handleCombatDamageAssigned(gameData, player, attackerIndex, assignments);
    }


    /**
     * Resets all combat-related state on permanents and game data.
     */
    public void clearCombatState(GameData gameData) {
        gameData.forEachBattlefield((playerId, battlefield) -> battlefield.forEach(permanent -> {
            // "Last combat" means the controller's own most recent combat (Territorial Hellkite), so an
            // opponent's combat does not roll the record over.
            if (playerId.equals(gameData.activePlayerId)) {
                permanent.rollOverCombatAttackRecord();
            }
            permanent.clearCombatState();
            permanent.clearUntilEndOfCombatAnimation();
        }));
        gameData.declaredAttackerIdsThisCombat.clear();
        gameData.creaturesThatEnlistedThisCombat.clear();
        gameData.ragingRiverBlockRestrictionsThisCombat.clear();
        gameData.combatDamagePlayerAssignments.clear();
        gameData.combatSharedDamagePreventionAllocations.clear();
        gameData.combatDamageAssignmentModeChoices.clear();
        gameData.combatDamagePendingIndices.clear();
        gameData.combatDamageBlockerAssignments.clear();
        gameData.combatDamagePendingBlockerIndices.clear();
        gameData.combatDamageFirstStrikeAssignmentPhase = false;
        gameData.combatDamageFirstestStrikeAssignmentPhase = false;
        gameData.combatDamageFirstestStrikeStepComplete = false;
        gameData.creaturesInFirstestStrikeDamageStepThisCombat.clear();
        gameData.combatDamageFirstStrikeStepComplete = false;
        gameData.combatDamagePhase1Complete = false;
        gameData.combatDamagePhase1State = null;
        // Keep Master Warcraft's declaration control through additional combats this turn.
        gameData.clearDelayedActions(DelayedBlockerDeclarationControl.class,
                control -> !control.untilEndOfTurn());
    }

    /** Idempotent grant of Dread Wight's "{4}: Remove a paralyzation counter from this creature." */
    public static void grantParalyzationRemoveAbility(Permanent perm) {
        boolean alreadyGranted = perm.getPersistentGrantedActivatedAbilities().stream()
                .anyMatch(a -> a.getEffects().stream().anyMatch(e ->
                        e.equals(REMOVE_PARALYZATION_COUNTER_EFFECT)));
        if (alreadyGranted) {
            return;
        }
        perm.getPersistentGrantedActivatedAbilities().add(new ActivatedAbility(
                false,
                "{4}",
                List.of(REMOVE_PARALYZATION_COUNTER_EFFECT),
                "{4}: Remove a paralyzation counter from this creature."));
    }
}
