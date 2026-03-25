package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.AttackTarget;
import com.github.laxika.magicalvibes.networking.message.AvailableBlockersMessage;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    // ===== Attack delegation =====

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

    // ===== Block delegation =====

    public List<Integer> getBlockableCreatureIndices(GameData gameData, UUID playerId) {
        return combatBlockService.getBlockableCreatureIndices(gameData, playerId);
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

    // ===== Damage delegation =====

    public CombatResult resolveCombatDamage(GameData gameData) {
        return combatDamageService.resolveCombatDamage(gameData);
    }

    public void handleCombatDamageAssigned(GameData gameData, Player player, int attackerIndex, Map<UUID, Integer> assignments) {
        combatDamageService.handleCombatDamageAssigned(gameData, player, attackerIndex, assignments);
    }

    // ===== Combat state management =====

    /**
     * Resets all combat-related state on permanents and game data.
     */
    public void clearCombatState(GameData gameData) {
        gameData.forEachBattlefield((playerId, battlefield) ->
                battlefield.forEach(Permanent::clearCombatState));
        gameData.combatDamagePlayerAssignments.clear();
        gameData.combatDamagePendingIndices.clear();
        gameData.combatDamagePhase1Complete = false;
        gameData.combatDamagePhase1State = null;
    }

    /**
     * Sacrifices all permanents marked for end-of-combat sacrifice.
     */
    public void processEndOfCombatSacrifices(GameData gameData) {
        gameData.forEachBattlefield((playerId, battlefield) -> {
            List<Permanent> toSacrifice = battlefield.stream()
                    .filter(p -> gameData.permanentsToSacrificeAtEndOfCombat.contains(p.getId()))
                    .toList();
            for (Permanent perm : toSacrifice) {
                permanentRemovalService.removePermanentToGraveyard(gameData, perm);
                String logEntry = perm.getCard().getName() + " is sacrificed.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} sacrificed at end of combat", gameData.id, perm.getCard().getName());
            }
        });
        gameData.permanentsToSacrificeAtEndOfCombat.clear();
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * Exiles all tokens marked for end-of-combat exile (e.g. Geist of Saint Traft's Angel token).
     */
    public void processEndOfCombatExiles(GameData gameData) {
        gameData.forEachBattlefield((playerId, battlefield) -> {
            List<Permanent> toExile = battlefield.stream()
                    .filter(p -> gameData.pendingTokenExilesAtEndOfCombat.contains(p.getId()))
                    .toList();
            for (Permanent perm : toExile) {
                permanentRemovalService.removePermanentToExile(gameData, perm);
                String logEntry = perm.getCard().getName() + " token is exiled.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} exiled at end of combat", gameData.id, perm.getCard().getName());
            }
        });
        gameData.pendingTokenExilesAtEndOfCombat.clear();
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * Destroys all Equipment attached to creatures marked for end-of-combat equipment destruction
     * (e.g. by Corrosive Ooze's trigger). Respects indestructible via
     * {@link PermanentRemovalService#tryDestroyPermanent}.
     */
    public void processEndOfCombatEquipmentDestruction(GameData gameData) {
        Set<UUID> creatureIds = new HashSet<>(gameData.creaturesWithEquipmentToDestroyAtEndOfCombat);
        gameData.creaturesWithEquipmentToDestroyAtEndOfCombat.clear();

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
     * Exiles all permanents marked for end-of-combat exile-and-return-transformed
     * (e.g. Conqueror's Galleon). Each permanent is exiled and immediately returned
     * to the battlefield transformed (as its back face) under its controller's control.
     */
    public void processEndOfCombatExileAndReturnTransformed(GameData gameData) {
        Set<UUID> toProcess = new HashSet<>(gameData.pendingExileAndReturnTransformedAtEndOfCombat);
        gameData.pendingExileAndReturnTransformedAtEndOfCombat.clear();

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
