package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.model.action.ExileAndReturnTransformedAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.DestroyCombatOpponentsAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.DestroyEquipmentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.DealDamageToPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.DelayedBlockerDeclarationControl;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.action.DelayedUnblockedAttackerUntapRemoveFromCombat;
import com.github.laxika.magicalvibes.model.action.GainControlOfPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.PutCounterOnPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.PutMinusOneCounterAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.RemoveCounterFromSourceAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.SacrificeAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.PhaseOutAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.TapAndSkipUntapAtEndOfCombat;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CombatAttackTarget;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.ExileAndReturnTransformedService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.combat.attack.AttackLegalityService;
import com.github.laxika.magicalvibes.service.combat.attack.CombatAttackService;
import com.github.laxika.magicalvibes.service.combat.block.CombatBlockService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentControlSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.TapUntapSupport;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.turn.PhasingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    private final CreatureControlService creatureControlService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PermanentControlSupport permanentControlSupport;
    private final DamageSupport damageSupport;
    private final StateBasedActionService stateBasedActionService;
    private final TapUntapSupport tapUntapSupport;
    private final ExileAndReturnTransformedService exileAndReturnTransformedService;
    private final PhasingService phasingService;

    /** Layer-2 control effect wrapping each end-of-combat control gain (drives layer classification). */
    private static final GainControlOfTargetEffect CONTROL_OPPONENT_EFFECT =
            new GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD);


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
        gameData.forEachBattlefield((playerId, battlefield) ->
                battlefield.forEach(Permanent::clearCombatState));
        gameData.combatDamagePlayerAssignments.clear();
        gameData.combatDamagePendingIndices.clear();
        gameData.combatDamageBlockerAssignments.clear();
        gameData.combatDamagePendingBlockerIndices.clear();
        gameData.combatDamageFirstStrikeAssignmentPhase = false;
        gameData.combatDamageFirstStrikeStepComplete = false;
        gameData.combatDamagePhase1Complete = false;
        gameData.combatDamagePhase1State = null;
        // Melee's two combat-scoped delayed abilities ("this combat") expire here.
        gameData.clearDelayedActions(DelayedBlockerDeclarationControl.class);
        gameData.clearDelayedActions(DelayedUnblockedAttackerUntapRemoveFromCombat.class);
    }

    /**
     * Sacrifices all permanents marked for end-of-combat sacrifice.
     */
    public void processEndOfCombatSacrifices(GameData gameData) {
        List<SacrificeAtEndOfCombat> actions = gameData.drainDelayedActions(SacrificeAtEndOfCombat.class);
        for (SacrificeAtEndOfCombat action : actions) {
            Permanent perm = gameQueryService.findPermanentById(gameData, action.permanentId());
            // "sacrifice it and it deals N damage to you" (Time Elemental): the damage is a delayed
            // triggered ability that fires even if the creature already left the battlefield (last-known
            // information). Deal it before the sacrifice so source-based prevention still sees the source.
            if (action.damageToController() > 0 && action.controllerId() != null) {
                Card source = perm != null ? perm.getCard() : action.sourceCard();
                if (source != null) {
                    StackEntry damageEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source,
                            action.controllerId(), source.getName(), List.<CardEffect>of(),
                            (UUID) null, action.permanentId());
                    damageSupport.dealDamageToPlayer(gameData, damageEntry, action.controllerId(),
                            action.damageToController());
                }
            }
            if (perm != null) {
                UUID sacrificingPlayerId = gameQueryService.findPermanentController(gameData, action.permanentId());
                permanentRemovalService.removePermanentToGraveyard(gameData, perm);
                gameLogService.append(gameData, GameLog.isSacrificed(perm.getCard()));
                log.info("Game {} - {} sacrificed at end of combat", gameData.id, perm.getCard().getName());
                // "If the player does, they create a … token" (Basalt Golem) — only on an actual sacrifice.
                if (action.tokenForSacrificingPlayer() != null && sacrificingPlayerId != null) {
                    permanentControlSupport.applyCreateToken(gameData, sacrificingPlayerId,
                            action.tokenForSacrificingPlayer(),
                            action.sourceCard() != null ? action.sourceCard().getSetCode() : null);
                }
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * Taps every creature scheduled by Joven's Ferrets and increments its skip-untap count, so it
     * misses its controller's next untap step. Creatures that already left the battlefield are
     * skipped; an already-tapped creature still picks up the untap lock.
     */
    public void processEndOfCombatTaps(GameData gameData) {
        List<TapAndSkipUntapAtEndOfCombat> actions =
                gameData.drainDelayedActions(TapAndSkipUntapAtEndOfCombat.class);
        for (TapAndSkipUntapAtEndOfCombat action : actions) {
            Permanent perm = gameQueryService.findPermanentById(gameData, action.permanentId());
            if (perm == null) {
                continue;
            }
            tapUntapSupport.tapPermanent(gameData, perm);
            perm.setSkipUntapCount(perm.getSkipUntapCount() + 1);
            gameLogService.append(gameData, GameLog.cardThen(perm.getCard(),
                    " is tapped and doesn't untap during its controller's next untap step."));
            log.info("Game {} - {} tapped and untap-locked at end of combat", gameData.id,
                    perm.getCard().getName());
        }
    }

    /**
     * Phases out every permanent scheduled for end-of-combat phasing (e.g. by Teferi's Veil's
     * "whenever a creature you control attacks, it phases out at end of combat"). Attachments follow
     * indirectly (CR 702.26g) and each permanent is removed from combat (CR 506.4); because it
     * phased out directly it phases in during its controller's next untap step (CR 702.26a).
     */
    public void processEndOfCombatPhaseOuts(GameData gameData) {
        List<Permanent> phasingOut = gameData.drainDelayedActions(PhaseOutAtEndOfCombat.class).stream()
                .map(action -> gameQueryService.findPermanentById(gameData, action.permanentId()))
                .filter(Objects::nonNull)
                .toList();
        if (phasingOut.isEmpty()) {
            return;
        }
        phasingService.phaseOut(gameData, phasingOut);
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
     * Destroys all Equipment attached to creatures marked for end-of-combat equipment destruction
     * (e.g. by Corrosive Ooze's trigger). Respects indestructible via
     * {@link PermanentRemovalService#tryDestroyPermanent}.
     */
    public void processEndOfCombatEquipmentDestruction(GameData gameData) {
        List<UUID> creatureIds = gameData.drainDelayedActions(DestroyEquipmentAtEndOfCombat.class).stream()
                .map(DestroyEquipmentAtEndOfCombat::creatureId)
                .toList();

        for (UUID creatureId : creatureIds) {
            List<Permanent> equipmentToDestroy = new ArrayList<>();
            gameData.forEachPermanent((playerId, p) -> {
                if (creatureId.equals(p.getAttachedTo())
                        && p.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                    equipmentToDestroy.add(p);
                }
            });

            for (Permanent equipment : equipmentToDestroy) {
                if (permanentRemovalService.tryDestroyPermanent(gameData, equipment)) {
                    gameLogService.append(gameData, GameLog.isDestroyed(equipment.getCard()));
                    log.info("Game {} - {} destroyed at end of combat (equipment destruction)",
                            gameData.id, equipment.getCard().getName());
                }
            }
        }
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
     * Destroys, for each creature scheduled by Venomous Breath, every creature that blocked or was
     * blocked by it this turn. The opponent set is read here rather than at spell resolution, so
     * blocks declared after the spell resolved are included. Respects indestructible and
     * regeneration via {@link PermanentRemovalService#tryDestroyPermanent}.
     */
    public void processEndOfCombatCombatOpponentDestructions(GameData gameData) {
        List<DestroyCombatOpponentsAtEndOfCombat> scheduled =
                gameData.drainDelayedActions(DestroyCombatOpponentsAtEndOfCombat.class);
        for (DestroyCombatOpponentsAtEndOfCombat action : scheduled) {
            Set<UUID> opponentIds = gameData.combatBlockOpponentIdsThisTurn.get(action.creatureId());
            if (opponentIds == null) {
                continue;
            }
            for (UUID opponentId : opponentIds) {
                Permanent opponent = gameQueryService.findPermanentById(gameData, opponentId);
                if (opponent == null) {
                    continue;
                }
                if (permanentRemovalService.tryDestroyPermanent(gameData, opponent, false)) {
                    gameLogService.append(gameData, GameLog.isDestroyed(opponent.getCard()));
                    log.info("Game {} - {} destroyed at end of combat (Venomous Breath)",
                            gameData.id, opponent.getCard().getName());
                }
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * Puts -1/-1 counters on all permanents scheduled for end-of-combat counter placement (e.g. by
     * Wicker Warcrawler's "whenever this creature attacks or blocks, put a -1/-1 counter on it at
     * end of combat"). Respects {@code cantHaveCounters}/{@code cantHaveMinusOneMinusOneCounters}
     * and fires "whenever a -1/-1 counter is put on a creature" triggers.
     */
    public void processEndOfCombatSourceCounters(GameData gameData) {
        List<PutMinusOneCounterAtEndOfCombat> toCounter =
                gameData.drainDelayedActions(PutMinusOneCounterAtEndOfCombat.class);
        for (PutMinusOneCounterAtEndOfCombat action : toCounter) {
            Permanent perm = gameQueryService.findPermanentById(gameData, action.permanentId());
            if (perm == null || action.amount() <= 0) {
                continue;
            }
            if (gameQueryService.cantHaveCounters(gameData, perm)
                    || gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, perm)) {
                continue;
            }
            int counters = gameQueryService.reduceMinusOneMinusOneCounters(gameData, perm, action.amount());
            if (counters <= 0) {
                continue;
            }
            perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE,
                    perm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + counters);
            gameLogService.append(gameData, GameLog.cardThen(perm.getCard(),
                    " gets " + counters + " -1/-1 counter(s)."));
            log.info("Game {} - {} gets {} -1/-1 counter(s) at end of combat",
                    gameData.id, perm.getCard().getName(), counters);
            // The permanent's controller is the player putting these self-counters (Nest of Scarabs).
            UUID counterPlacerId = gameQueryService.findPermanentController(gameData, perm.getId());
            permanentCounterSupport.fireMinusOneMinusOneCounterPutOnCreatureTriggers(gameData, perm, counters, counterPlacerId);
        }
    }

    /**
     * Puts the scheduled counters on all permanents marked for end-of-combat counter placement on a
     * combat opponent (e.g. Greater Werewolf's "put a -0/-2 counter on each creature blocking or
     * blocked by this creature") — also used for the source itself (Kjeldoran Home Guard), including
     * its "and create a … token" rider. Respects {@code cantHaveCounters}. When {@code alsoTap} is set
     * the permanent is tapped (Dread Wight). Paralyzation counters also grant
     * "{4}: Remove a paralyzation counter from this creature" for as long as the permanent remains
     * (source-independent — survives the creating creature leaving).
     */
    public void processEndOfCombatOpponentCounters(GameData gameData) {
        List<PutCounterOnPermanentAtEndOfCombat> toCounter =
                gameData.drainDelayedActions(PutCounterOnPermanentAtEndOfCombat.class);
        for (PutCounterOnPermanentAtEndOfCombat action : toCounter) {
            Permanent perm = gameQueryService.findPermanentById(gameData, action.permanentId());
            if (perm == null || action.amount() <= 0) {
                continue;
            }
            // One trigger does both, so the token is created even when the counter can't be placed.
            if (action.tokenForController() != null) {
                UUID controllerId = gameQueryService.findPermanentController(gameData, perm.getId());
                if (controllerId != null) {
                    permanentControlSupport.applyCreateToken(gameData, controllerId,
                            action.tokenForController(), perm.getCard().getSetCode());
                }
            }
            if (gameQueryService.cantHaveCounters(gameData, perm)) {
                continue;
            }
            perm.setCounterCount(action.counterType(),
                    perm.getCounterCount(action.counterType()) + action.amount());
            if (action.alsoTap()) {
                perm.tap();
            }
            if (action.counterType() == CounterType.PARALYZATION) {
                grantParalyzationRemoveAbility(perm);
            }
            String tapText = action.alsoTap() ? " and becomes tapped" : "";
            gameLogService.append(gameData, GameLog.cardThen(perm.getCard(),
                    " gets " + action.amount() + " counter(s)" + tapText + "."));
            log.info("Game {} - {} gets {} {} counter(s){} at end of combat",
                    gameData.id, perm.getCard().getName(), action.amount(), action.counterType(),
                    action.alsoTap() ? " and is tapped" : "");
        }
    }

    /** Idempotent grant of Dread Wight's "{4}: Remove a paralyzation counter from this creature." */
    private static void grantParalyzationRemoveAbility(Permanent perm) {
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
     * Deals the scheduled damage to all permanents marked for end-of-combat damage (Dwarven Sea
     * Clan's "This creature deals 2 damage to that creature at end of combat"). The source card is
     * carried on the action, so the damage is still dealt with last-known information when the
     * source already left the battlefield. Lethal damage is cleaned up by the caller's state-based
     * action check.
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
                    action.controllerId(), action.sourceCard().getName(), List.<CardEffect>of(),
                    action.permanentId(), action.sourcePermanentId());
            damageSupport.resolveCreatureTargetDamage(gameData, damageEntry, action.damage());
        }
        stateBasedActionService.performStateBasedActions(gameData);
    }

    /**
     * Removes the scheduled counters from all permanents marked for end-of-combat counter removal
     * (e.g. Clockwork Beast's "At end of combat, if this creature attacked or blocked this combat,
     * remove a +1/+0 counter from it"). Clamped at zero — a permanent with none is unaffected.
     */
    public void processEndOfCombatCounterRemovals(GameData gameData) {
        List<RemoveCounterFromSourceAtEndOfCombat> toRemove =
                gameData.drainDelayedActions(RemoveCounterFromSourceAtEndOfCombat.class);
        for (RemoveCounterFromSourceAtEndOfCombat action : toRemove) {
            Permanent perm = gameQueryService.findPermanentById(gameData, action.permanentId());
            if (perm == null || action.amount() <= 0) {
                continue;
            }
            int current = perm.getCounterCount(action.counterType());
            if (current <= 0) {
                continue;
            }
            int removed = Math.min(action.amount(), current);
            perm.setCounterCount(action.counterType(), current - removed);
            gameLogService.append(gameData, GameLog.cardThen(perm.getCard(),
                    " loses " + removed + " counter(s)."));
            log.info("Game {} - {} loses {} {} counter(s) at end of combat",
                    gameData.id, perm.getCard().getName(), removed, action.counterType());
        }
    }

    /**
     * Gains control of all permanents scheduled for end-of-combat control change (e.g. The Wretched's
     * "At end of combat, gain control of all creatures blocking this creature for as long as you
     * control this creature"). Control is applied with {@code WHILE_SOURCE_ON_BATTLEFIELD} keyed to
     * the source, so it ends when the source leaves the battlefield or its controller loses it.
     */
    public void processEndOfCombatControlGains(GameData gameData) {
        List<GainControlOfPermanentAtEndOfCombat> toControl =
                gameData.drainDelayedActions(GainControlOfPermanentAtEndOfCombat.class);
        for (GainControlOfPermanentAtEndOfCombat action : toControl) {
            Permanent target = gameQueryService.findPermanentById(gameData, action.permanentId());
            if (target == null) {
                continue;
            }
            // A source-linked duration needs the source still on the battlefield under the gaining
            // player; a permanent gain is independent of the source.
            if (action.duration().isSourceLinked()) {
                Permanent source = gameQueryService.findPermanentById(gameData, action.sourcePermanentId());
                if (source == null
                        || !action.newControllerId().equals(gameData.findControllerOf(source))) {
                    continue;
                }
            }
            creatureControlService.applyControlEffect(gameData, action.newControllerId(), target,
                    CONTROL_OPPONENT_EFFECT, action.duration().toEffectDuration(),
                    action.sourcePermanentId(), action.sourceCardName());
        }
    }

    /**
     * Exiles all permanents marked for end-of-combat exile-and-return-transformed
     * (e.g. Conqueror's Galleon). Each permanent is exiled and immediately returned
     * to the battlefield transformed (as its back face) under its controller's control.
     */
    public void processEndOfCombatExileAndReturnTransformed(GameData gameData) {
        List<UUID> toProcess = gameData.drainDelayedActions(ExileAndReturnTransformedAtEndOfCombat.class).stream()
                .map(ExileAndReturnTransformedAtEndOfCombat::permanentId)
                .toList();

        for (UUID permId : toProcess) {
            exileAndReturnTransformedService.exileAndReturnTransformed(gameData, permId);
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
