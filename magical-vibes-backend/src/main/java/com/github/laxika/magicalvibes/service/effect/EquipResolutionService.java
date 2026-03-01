package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOnUnattachEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EquipResolutionService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;

    @HandlesEffect(EquipEffect.class)
    private void resolveEquip(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            String logEntry = entry.getCard().getName() + "'s equip ability fizzles (target creature no longer exists).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Equip fizzles, target creature left battlefield", gameData.id);
            return;
        }

        // Find the equipment permanent on the battlefield by matching the card
        Permanent equipment = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (p.getCard().getId().equals(entry.getCard().getId())) {
                    equipment = p;
                    break;
                }
            }
            if (equipment != null) break;
        }

        if (equipment == null) {
            String logEntry = entry.getCard().getName() + "'s equip ability fizzles (equipment no longer on the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Equip fizzles, equipment left battlefield", gameData.id);
            return;
        }

        // Check for sacrifice-on-unattach before moving the equipment
        UUID oldAttachedTo = equipment.getAttachedTo();
        boolean hasSacrificeOnUnattach = equipment.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof SacrificeOnUnattachEffect);

        equipment.setAttachedTo(target.getId());

        String logEntry = entry.getCard().getName() + " is now attached to " + target.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} equipped to {}", gameData.id, entry.getCard().getName(), target.getCard().getName());

        // Sacrifice the previously-equipped creature if the equipment has SacrificeOnUnattachEffect
        if (hasSacrificeOnUnattach && oldAttachedTo != null && !oldAttachedTo.equals(target.getId())) {
            Permanent oldCreature = gameQueryService.findPermanentById(gameData, oldAttachedTo);
            if (oldCreature != null) {
                String sacrificeLog = oldCreature.getCard().getName() + " is sacrificed (" + equipment.getCard().getName() + " became unattached).";
                gameBroadcastService.logAndBroadcast(gameData, sacrificeLog);
                log.info("Game {} - {} sacrificed due to {} unattach", gameData.id, oldCreature.getCard().getName(), equipment.getCard().getName());
                permanentRemovalService.removePermanentToGraveyard(gameData, oldCreature);
                permanentRemovalService.removeOrphanedAuras(gameData);
            }
        }
    }

    @HandlesEffect(AttachTargetEquipmentToTargetCreatureEffect.class)
    private void resolveAttachTargetEquipmentToTargetCreature(GameData gameData, StackEntry entry) {
        List<UUID> targets = entry.getTargetPermanentIds();
        if (targets == null || targets.size() < 2) {
            String logEntry = entry.getCard().getName() + "'s ability fizzles (invalid targets).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Attach equipment ability fizzles, insufficient targets", gameData.id);
            return;
        }

        UUID equipmentId = targets.get(0);
        UUID creatureId = targets.get(1);

        Permanent equipment = gameQueryService.findPermanentById(gameData, equipmentId);
        if (equipment == null) {
            String logEntry = entry.getCard().getName() + "'s ability fizzles (equipment no longer on the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Attach equipment ability fizzles, equipment left battlefield", gameData.id);
            return;
        }

        Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
        if (creature == null) {
            String logEntry = entry.getCard().getName() + "'s ability fizzles (target creature no longer on the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Attach equipment ability fizzles, target creature left battlefield", gameData.id);
            return;
        }

        // Check for sacrifice-on-unattach before moving the equipment
        UUID oldAttachedTo = equipment.getAttachedTo();
        boolean hasSacrificeOnUnattach = equipment.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof SacrificeOnUnattachEffect);

        equipment.setAttachedTo(creature.getId());

        String logEntry = equipment.getCard().getName() + " is now attached to " + creature.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} attached to {} via {}", gameData.id, equipment.getCard().getName(), creature.getCard().getName(), entry.getCard().getName());

        // Sacrifice the previously-equipped creature if the equipment has SacrificeOnUnattachEffect
        if (hasSacrificeOnUnattach && oldAttachedTo != null && !oldAttachedTo.equals(creature.getId())) {
            Permanent oldCreature = gameQueryService.findPermanentById(gameData, oldAttachedTo);
            if (oldCreature != null) {
                String sacrificeLog = oldCreature.getCard().getName() + " is sacrificed (" + equipment.getCard().getName() + " became unattached).";
                gameBroadcastService.logAndBroadcast(gameData, sacrificeLog);
                log.info("Game {} - {} sacrificed due to {} unattach", gameData.id, oldCreature.getCard().getName(), equipment.getCard().getName());
                permanentRemovalService.removePermanentToGraveyard(gameData, oldCreature);
                permanentRemovalService.removeOrphanedAuras(gameData);
            }
        }
    }
}

