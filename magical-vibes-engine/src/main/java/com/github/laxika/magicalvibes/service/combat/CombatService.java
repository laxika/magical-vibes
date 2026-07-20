package com.github.laxika.magicalvibes.service.combat;
import com.github.laxika.magicalvibes.model.action.ExileAndReturnTransformedAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.DestroyEquipmentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.action.GainControlOfPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.PutCounterOnPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.PutMinusOneCounterAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.RemoveCounterFromSourceAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.SacrificeAtEndOfCombat;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
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
import com.github.laxika.magicalvibes.networking.message.AttackTarget;
import com.github.laxika.magicalvibes.networking.message.AvailableBlockersMessage;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
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
 *   <li>{@link CombatBlockService} — blocker declaration and validation</li>
 *   <li>{@link CombatDamageService} — damage calculation, assignment, and triggers</li>
 *   <li>{@link CombatTriggerService} — shared trigger helpers (aura triggers, APNAP ordering)</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CombatService {

    private final CombatAttackService combatAttackService;
    private final CombatBlockService combatBlockService;
    private final CombatDamageService combatDamageService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final CreatureControlService creatureControlService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final DamageSupport damageSupport;

    /** Layer-2 control effect wrapping each end-of-combat control gain (drives layer classification). */
    private static final GainControlOfTargetEffect CONTROL_OPPONENT_EFFECT =
            new GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD);


    public List<Integer> getAttackableCreatureIndices(GameData gameData, UUID playerId) {
        return combatAttackService.getAttackableCreatureIndices(gameData, playerId);
    }

    public List<Integer> getMustAttackIndices(GameData gameData, UUID playerId, List<Integer> attackableIndices) {
        return combatAttackService.getMustAttackIndices(gameData, playerId, attackableIndices);
    }

    public List<AttackTarget> buildAvailableTargets(GameData gameData, UUID activePlayerId) {
        return combatAttackService.buildAvailableTargets(gameData, activePlayerId);
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

    public AvailableBlockersMessage buildAvailableBlockersMessage(GameData gameData,
                                                                   List<Integer> blockable,
                                                                   List<Integer> attackerIndices,
                                                                   UUID defenderId,
                                                                   UUID activeId) {
        return combatBlockService.buildAvailableBlockersMessage(gameData, blockable, attackerIndices, defenderId, activeId);
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
                permanentRemovalService.removePermanentToGraveyard(gameData, perm);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.isSacrificed(perm.getCard()));
                log.info("Game {} - {} sacrificed at end of combat", gameData.id, perm.getCard().getName());
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
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.isDestroyed(equipment.getCard()));
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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(perm.getCard(),
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
     * blocked by this creature"). Respects {@code cantHaveCounters}.
     */
    public void processEndOfCombatOpponentCounters(GameData gameData) {
        List<PutCounterOnPermanentAtEndOfCombat> toCounter =
                gameData.drainDelayedActions(PutCounterOnPermanentAtEndOfCombat.class);
        for (PutCounterOnPermanentAtEndOfCombat action : toCounter) {
            Permanent perm = gameQueryService.findPermanentById(gameData, action.permanentId());
            if (perm == null || action.amount() <= 0) {
                continue;
            }
            if (gameQueryService.cantHaveCounters(gameData, perm)) {
                continue;
            }
            perm.setCounterCount(action.counterType(),
                    perm.getCounterCount(action.counterType()) + action.amount());
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(perm.getCard(),
                    " gets " + action.amount() + " counter(s)."));
            log.info("Game {} - {} gets {} {} counter(s) at end of combat",
                    gameData.id, perm.getCard().getName(), action.amount(), action.counterType());
        }
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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(perm.getCard(),
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
            // Source must still be on the battlefield and controlled by the gaining player.
            Permanent source = gameQueryService.findPermanentById(gameData, action.sourcePermanentId());
            if (source == null
                    || !action.newControllerId().equals(gameData.findControllerOf(action.sourcePermanentId()))) {
                continue;
            }
            creatureControlService.applyControlEffect(gameData, action.newControllerId(), target,
                    CONTROL_OPPONENT_EFFECT, ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD.toEffectDuration(),
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
            // Find the permanent and its controller
            Permanent perm = null;
            UUID controllerId = null;
            for (Map.Entry<UUID, List<Permanent>> entry : gameData.playerBattlefields.entrySet()) {
                for (Permanent p : entry.getValue()) {
                    if (p.getId().equals(permId)) {
                        perm = p;
                        controllerId = entry.getKey();
                        break;
                    }
                }
                if (perm != null) break;
            }
            if (perm == null) continue; // Already left the battlefield

            Card originalCard = perm.getOriginalCard();
            Card backFace = originalCard.getBackFaceCard();
            if (backFace == null) continue;

            // Exile the permanent
            permanentRemovalService.removePermanentToExile(gameData, perm);
            // Remove from exile immediately (it returns right away as the back face)
            gameData.removeFromExile(originalCard.getId());

            // Create new permanent from original card, then transform to back face
            Permanent newPerm = new Permanent(originalCard);
            newPerm.setCard(backFace);
            newPerm.setTransformed(true);
            newPerm.setSummoningSick(false); // Lands don't have summoning sickness

            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, newPerm);

            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(originalCard,
                    " is exiled and returns transformed as ", backFace, "."));
            log.info("Game {} - {} exiled and returned transformed as {}",
                    gameData.id, originalCard.getName(), backFace.getName());
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
