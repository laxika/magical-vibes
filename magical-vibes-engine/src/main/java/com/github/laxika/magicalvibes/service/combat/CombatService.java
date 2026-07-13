package com.github.laxika.magicalvibes.service.combat;
import com.github.laxika.magicalvibes.model.action.ExileAndReturnTransformedAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.DestroyEquipmentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.DestroyPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.ExileTokenAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.PutMinusOneCounterAtEndOfCombat;
import com.github.laxika.magicalvibes.model.action.SacrificeAtEndOfCombat;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.AttackTarget;
import com.github.laxika.magicalvibes.networking.message.AvailableBlockersMessage;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final PermanentCounterSupport permanentCounterSupport;


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
        return combatAttackService.declareAttackers(gameData, player, attackerIndices, attackTargets);
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
        gameData.combatDamageFirstStrikeStepComplete = false;
        gameData.combatDamagePhase1Complete = false;
        gameData.combatDamagePhase1State = null;
    }

    /**
     * Sacrifices all permanents marked for end-of-combat sacrifice.
     */
    public void processEndOfCombatSacrifices(GameData gameData) {
        Set<UUID> toSacrificeIds = gameData.drainDelayedActions(SacrificeAtEndOfCombat.class).stream()
                .map(SacrificeAtEndOfCombat::permanentId)
                .collect(Collectors.toSet());
        gameData.forEachBattlefield((playerId, battlefield) -> {
            List<Permanent> toSacrifice = battlefield.stream()
                    .filter(p -> toSacrificeIds.contains(p.getId()))
                    .toList();
            for (Permanent perm : toSacrifice) {
                permanentRemovalService.removePermanentToGraveyard(gameData, perm);
                String logEntry = perm.getCard().getName() + " is sacrificed.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} sacrificed at end of combat", gameData.id, perm.getCard().getName());
            }
        });
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * Exiles all tokens marked for end-of-combat exile (e.g. Geist of Saint Traft's Angel token).
     */
    public void processEndOfCombatExiles(GameData gameData) {
        Set<UUID> toExileIds = gameData.drainDelayedActions(ExileTokenAtEndOfCombat.class).stream()
                .map(ExileTokenAtEndOfCombat::permanentId)
                .collect(Collectors.toSet());
        gameData.forEachBattlefield((playerId, battlefield) -> {
            List<Permanent> toExile = battlefield.stream()
                    .filter(p -> toExileIds.contains(p.getId()))
                    .toList();
            for (Permanent perm : toExile) {
                permanentRemovalService.removePermanentToExile(gameData, perm);
                String logEntry = perm.getCard().getName() + " token is exiled.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} exiled at end of combat", gameData.id, perm.getCard().getName());
            }
        });
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
                    String logEntry = equipment.getCard().getName() + " is destroyed.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
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
        List<DestroyPermanentAtEndOfCombat> toDestroy =
                gameData.drainDelayedActions(DestroyPermanentAtEndOfCombat.class);
        for (DestroyPermanentAtEndOfCombat action : toDestroy) {
            Permanent perm = gameData.playerBattlefields.values().stream()
                    .flatMap(List::stream)
                    .filter(p -> p.getId().equals(action.permanentId()))
                    .findFirst()
                    .orElse(null);
            if (perm == null) {
                continue;
            }
            if (permanentRemovalService.tryDestroyPermanent(gameData, perm, action.cannotBeRegenerated())) {
                String logEntry = perm.getCard().getName() + " is destroyed.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} destroyed at end of combat", gameData.id, perm.getCard().getName());
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
            perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE,
                    perm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + action.amount());
            String logEntry = perm.getCard().getName() + " gets " + action.amount() + " -1/-1 counter(s).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} gets {} -1/-1 counter(s) at end of combat",
                    gameData.id, perm.getCard().getName(), action.amount());
            permanentCounterSupport.fireMinusOneMinusOneCounterPutOnCreatureTriggers(gameData, perm, action.amount());
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

            String logEntry = originalCard.getName() + " is exiled and returns transformed as " + backFace.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} exiled and returned transformed as {}",
                    gameData.id, originalCard.getName(), backFace.getName());
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
