package com.github.laxika.magicalvibes.service.aura;

import com.github.laxika.magicalvibes.service.DeathTriggerService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles aura and attachment lifecycle: removing orphaned auras when their target leaves the
 * battlefield, detaching equipment, and returning stolen creatures to their owners when the
 * controlling effect ends.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuraAttachmentService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GraveyardService graveyardService;
    private final DeathTriggerService deathTriggerService;

    /**
     * Removes auras whose enchanted permanent no longer exists and detaches equipment whose
     * equipped creature has left the battlefield (CR 303.4c, CR 301.5c). Orphaned auras are
     * put into their owner's graveyard; equipment simply becomes unattached. After cleanup,
     * also returns permanently-stolen creatures whose controlling effect has ended.
     *
     * @param gameData the current game state
     */
    public void removeOrphanedAuras(GameData gameData) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            Iterator<Permanent> it = battlefield.iterator();
            while (it.hasNext()) {
                Permanent p = it.next();
                if (p.isAttached()
                        && !gameData.playerIds.contains(p.getAttachedTo())
                        && gameQueryService.findPermanentById(gameData, p.getAttachedTo()) == null) {
                    if (p.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                        // Equipment stays on the battlefield unattached when the equipped creature leaves
                        p.setAttachedTo(null);
                        String logEntry = p.getCard().getName() + " becomes unattached (equipped creature left the battlefield).";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} unattached (equipped creature left)", gameData.id, p.getCard().getName());
                    } else {
                        it.remove();
                        boolean wentToGraveyard = graveyardService.addCardToGraveyard(gameData, playerId, p.getOriginalCard(), Zone.BATTLEFIELD);
                        String logEntry = p.getCard().getName() + " is put into the graveyard (enchanted creature left the battlefield).";
                        gameBroadcastService.logAndBroadcast(gameData, logEntry);
                        log.info("Game {} - {} removed (orphaned aura)", gameData.id, p.getCard().getName());
                        // Check for Tiana-style triggers (Aura put into graveyard from battlefield)
                        if (wentToGraveyard) {
                            deathTriggerService.checkAllyAuraOrEquipmentPutIntoGraveyardTriggers(gameData, p.getCard(), playerId);
                        }
                    }
                }
            }
        }
        returnStolenCreatures(gameData, false);
    }

    /**
     * Returns stolen creatures to their original owners when the controlling effect has ended.
     * Creatures kept by a {@link ControlEnchantedCreatureEffect} aura or an enchantment-dependent
     * steal that is still enchanted are not returned. Permanent control changes are also skipped.
     *
     * @param gameData              the current game state
     * @param includeUntilEndOfTurn if {@code true}, only processes until-end-of-turn steals
     *                              (e.g. Threaten); if {@code false}, only processes non-temporary steals
     */
    public void returnStolenCreatures(GameData gameData, boolean includeUntilEndOfTurn) {
        if (gameData.stolenCreatures.isEmpty()) return;

        Iterator<Map.Entry<UUID, UUID>> it = gameData.stolenCreatures.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, UUID> entry = it.next();
            UUID creatureId = entry.getKey();
            UUID ownerId = entry.getValue();

            // Always clean up tracking for creatures that no longer exist on the battlefield,
            // regardless of the steal type
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
            if (creature == null) {
                it.remove();
                clearStolenCreatureTracking(gameData, creatureId);
                continue;
            }

            boolean isUntilEndOfTurnSteal = gameData.untilEndOfTurnStolenCreatures.contains(creatureId);

            if (includeUntilEndOfTurn && !isUntilEndOfTurnSteal) {
                continue;
            }
            if (!includeUntilEndOfTurn && isUntilEndOfTurnSteal) {
                continue;
            }

            if (gameData.permanentControlStolenCreatures.contains(creatureId)) {
                continue;
            }

            // Check source-dependent steals ("for as long as you control [source]")
            UUID dependentSourceId = gameData.sourceDependentStolenCreatures.get(creatureId);
            if (dependentSourceId != null) {
                Permanent source = gameQueryService.findPermanentById(gameData, dependentSourceId);
                if (source != null) {
                    UUID sourceController = gameQueryService.findPermanentController(gameData, dependentSourceId);
                    UUID creatureController = gameQueryService.findPermanentController(gameData, creatureId);
                    if (sourceController != null && sourceController.equals(creatureController)) {
                        if (includeUntilEndOfTurn) {
                            gameData.untilEndOfTurnStolenCreatures.remove(creatureId);
                        }
                        continue;
                    }
                }
                gameData.sourceDependentStolenCreatures.remove(creatureId);
            }

            if (gameQueryService.hasAuraWithEffect(gameData, creature, ControlEnchantedCreatureEffect.class)) {
                if (includeUntilEndOfTurn) {
                    gameData.untilEndOfTurnStolenCreatures.remove(creatureId);
                }
                continue;
            }

            if (gameData.enchantmentDependentStolenCreatures.contains(creatureId)
                    && gameQueryService.isEnchanted(gameData, creature)) {
                if (includeUntilEndOfTurn) {
                    gameData.untilEndOfTurnStolenCreatures.remove(creatureId);
                }
                continue;
            }
            gameData.enchantmentDependentStolenCreatures.remove(creatureId);

            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> bf = gameData.playerBattlefields.get(pid);
                if (bf != null && bf.remove(creature)) {
                    gameData.playerBattlefields.get(ownerId).add(creature);
                    creature.setSummoningSick(true);

                    // Equipment that changes controllers must be unattached (CR 704.5p)
                    if (creature.isAttached()
                            && creature.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                        creature.setAttachedTo(null);
                        String unattachLog = creature.getCard().getName() + " becomes unattached.";
                        gameBroadcastService.logAndBroadcast(gameData, unattachLog);
                        log.info("Game {} - {} unattached on control change", gameData.id, creature.getCard().getName());
                    }

                    String ownerName = gameData.playerIdToName.get(ownerId);
                    String logEntry = creature.getCard().getName() + " returns to " + ownerName + "'s control.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} returns to {}'s control", gameData.id, creature.getCard().getName(), ownerName);
                    break;
                }
            }
            it.remove();
            clearStolenCreatureTracking(gameData, creatureId);
        }
    }

    private void clearStolenCreatureTracking(GameData gameData, UUID creatureId) {
        gameData.enchantmentDependentStolenCreatures.remove(creatureId);
        gameData.untilEndOfTurnStolenCreatures.remove(creatureId);
        gameData.permanentControlStolenCreatures.remove(creatureId);
        gameData.sourceDependentStolenCreatures.remove(creatureId);
    }
}

