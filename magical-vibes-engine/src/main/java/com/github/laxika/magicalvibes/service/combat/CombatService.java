package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.model.action.DestroyCombatOpponentsAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.TapCombatOpponentsAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.DealDamageToPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.DelayedBlockerDeclarationControl;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.action.DelayedUnblockedAttackerUntapRemoveFromCombat;
import com.github.laxika.magicalvibes.model.action.RemoveCounterFromSourceAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.SacrificeAtEndOfCombat;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CombatAttackTarget;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentsOfTargetAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.TapCombatOpponentsOfTargetAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
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

import java.util.ArrayList;
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
        gameData.ragingRiverBlockRestrictionsThisCombat.clear();
        gameData.combatDamagePlayerAssignments.clear();
        gameData.combatSharedDamagePreventionAllocations.clear();
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
        gameData.clearDelayedActions(DelayedUnblockedAttackerUntapRemoveFromCombat.class);
    }

    /**
     * Sacrifices all permanents marked for end-of-combat sacrifice.
     */
    public void processEndOfCombatSacrifices(GameData gameData) {
        List<SacrificeAtEndOfCombat> actions = gameData.drainDelayedActions(SacrificeAtEndOfCombat.class);
        for (SacrificeAtEndOfCombat action : actions) {
            Permanent perm = gameQueryService.findPermanentById(gameData, action.permanentId());
            Card source = action.sourceCard() != null ? action.sourceCard() : perm == null ? null : perm.getCard();
            if (action.tokenForSacrificingPlayer() != null && source != null
                    && action.controllerId() != null) {
                StackEntry delayed = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                        source, action.controllerId(), source.getName() + "'s delayed ability",
                        List.of(new com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect(
                                true, action.tokenForSacrificingPlayer())),
                        (UUID) null, action.permanentId());
                delayed.setNonTargeting(true);
                gameData.enqueueTrigger(delayed);
                continue;
            }
            if (action.damageToController() > 0 && action.controllerId() != null && source != null) {
                StackEntry delayed = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                        source, action.controllerId(), source.getName() + "'s delayed ability",
                        List.of(new com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect(),
                                new com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect(
                                        action.damageToController(),
                                        com.github.laxika.magicalvibes.model.effect.DamageRecipient.CONTROLLER)),
                        (UUID) null, action.permanentId());
                delayed.setNonTargeting(true);
                if (perm != null) delayed.setSourcePermanentSnapshot(new Permanent(perm));
                gameData.enqueueTrigger(delayed);
                continue;
            }
            if (perm != null && source != null) {
                UUID controllerId = action.controllerId() != null ? action.controllerId()
                        : gameQueryService.findPermanentController(gameData, action.permanentId());
                if (controllerId == null) continue;
                StackEntry delayed = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                        source, controllerId, source.getName() + "'s delayed ability",
                        List.of(new com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect()),
                        (UUID) null, action.permanentId());
                delayed.setNonTargeting(true);
                delayed.setSourcePermanentSnapshot(new Permanent(perm));
                gameData.enqueueTrigger(delayed);
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * Exiles all tokens marked for end-of-combat exile (e.g. Geist of Saint Traft's Angel token).
     */
    public void processEndOfCombatExiles(GameData gameData) {
        permanentRemovalService.processDelayedPermanentActions(gameData,
                DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT);
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * Destroys all permanents marked for end-of-combat destruction (e.g. by a Basilisk-style
     * "destroy that creature at end of combat" trigger such as Deathgazer). Respects indestructible
     * and, unless the scheduling effect set {@code cannotBeRegenerated}, regeneration shields via
     * {@link PermanentRemovalService#tryDestroyPermanent}.
     */
    public void processEndOfCombatDestructions(GameData gameData) {
        permanentRemovalService.processDelayedPermanentActions(gameData,
                DelayedPermanentActionKind.DESTROY_AT_END_OF_COMBAT);
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * Returns all permanents marked for end-of-combat bounce (e.g. by Kaijin of the Vanishing
     * Touch's block trigger) to their owners' hands. A permanent that already left the battlefield
     * is skipped.
     */
    public void processEndOfCombatReturnsToHand(GameData gameData) {
        permanentRemovalService.processDelayedPermanentActions(gameData,
                DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_OF_COMBAT);
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * Puts all permanents marked for an end-of-combat library-top move on top of their owners'
     * libraries. A permanent that already left the battlefield is skipped.
     */
    public void processEndOfCombatLibraryTucks(GameData gameData) {
        permanentRemovalService.processDelayedPermanentActions(gameData,
                DelayedPermanentActionKind.PUT_ON_TOP_OF_LIBRARY_AT_END_OF_COMBAT);
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * Puts the delayed "at end of combat, destroy the creatures that blocked or were blocked by it"
     * trigger on the stack for each scheduled directional or bidirectional combat-opponent
     * destruction. The trigger carries the opponent set captured when it was created.
     */
    public void processEndOfCombatCombatOpponentDestructions(GameData gameData) {
        List<DestroyCombatOpponentsAtEndOfCombat> scheduled =
                gameData.drainDelayedActions(DestroyCombatOpponentsAtEndOfCombat.class);
        for (DestroyCombatOpponentsAtEndOfCombat action : scheduled) {
            if (action.sourceCard() == null) {
                continue;
            }
            // "At end of combat, destroy ..." is a delayed triggered ability (CR 603.7), so it uses
            // the stack and players may respond before the creatures are destroyed.
            StackEntry trigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    action.sourceCard(),
                    action.controllerId(),
                    action.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(List.of(new DestroyCombatOpponentsOfTargetAtEndOfCombatEffect(
                            action.onlyCreaturesBlockedByTarget()))),
                    null,
                    action.creatureId());
            trigger.setNonTargeting(true);
            trigger.setTriggeringPermanentIds(List.copyOf(action.combatOpponentIds()));
            gameData.stack.add(trigger);
            gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(), "'s delayed trigger triggers."));
        }
    }

    /**
     * Puts Triton Tactics' delayed "at this turn's next end of combat, tap each creature that was
     * blocked by one of those creatures" trigger on the stack, carrying the chosen creatures as its
     * triggering permanents (CR 603.7).
     */
    public void processEndOfCombatCombatOpponentTaps(GameData gameData) {
        for (TapCombatOpponentsAtEndOfCombat action : gameData.drainDelayedActions(TapCombatOpponentsAtEndOfCombat.class)) {
            StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY, action.sourceCard(),
                    action.controllerId(), action.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(List.of(new TapCombatOpponentsOfTargetAtEndOfCombatEffect(false))),
                    (UUID) null, (UUID) null);
            trigger.setNonTargeting(true);
            trigger.setTriggeringPermanentIds(action.creatureIds());
            gameData.stack.add(trigger);
            gameLogService.append(gameData, GameLog.cardThen(action.sourceCard(), "'s delayed trigger triggers."));
        }
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

    /**
     * Queues delayed damage triggers for permanents marked for end-of-combat damage (Dwarven Sea
     * Clan's "This creature deals 2 damage to that creature at end of combat"). The source card is
     * carried on the action, so the damage is still dealt with last-known information when the
     * source already left the battlefield. Players may respond before each trigger resolves.
     */
    public void processEndOfCombatDamage(GameData gameData) {
        List<DealDamageToPermanentAtEndOfCombat> toDamage =
                gameData.drainDelayedActions(DealDamageToPermanentAtEndOfCombat.class);
        for (DealDamageToPermanentAtEndOfCombat action : toDamage) {
            Permanent target = gameQueryService.findPermanentById(gameData, action.permanentId());
            if (target == null || action.damage() <= 0 || action.sourceCard() == null) {
                continue;
            }
            StackEntry damageEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, action.sourceCard(),
                    action.controllerId(), action.sourceCard().getName(),
                    List.of(new com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect(
                            action.damage())), action.permanentId(), action.sourcePermanentId());
            damageEntry.setNonTargeting(true);
            gameData.enqueueTrigger(damageEntry);
        }
    }

    /**
     * Queues the delayed counter-removal abilities at end of combat so players can respond.
     */
    public void processEndOfCombatCounterRemovals(GameData gameData) {
        List<RemoveCounterFromSourceAtEndOfCombat> toRemove =
                gameData.drainDelayedActions(RemoveCounterFromSourceAtEndOfCombat.class);
        for (RemoveCounterFromSourceAtEndOfCombat action : toRemove) {
            Permanent perm = gameQueryService.findPermanentById(gameData, action.permanentId());
            if (perm == null || action.amount() <= 0) {
                continue;
            }
            StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(), gameQueryService.findPermanentController(gameData, perm.getId()),
                    perm.getCard().getName() + "'s delayed ability",
                    List.of(new com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect(
                            action.counterType(), action.amount())), null, perm.getId());
            trigger.setNonTargeting(true);
            trigger.setSourcePermanentSnapshot(new Permanent(perm));
            gameData.enqueueTrigger(trigger);
        }
    }
}
