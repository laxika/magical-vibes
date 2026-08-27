package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachTargetEquipmentToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final EquipSupport equipSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachTargetEquipmentToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var attachEffect = (AttachTargetEquipmentToTargetCreatureEffect) effect;
        int equipmentGroup = attachEffect.equipmentFirst() ? 0 : 1;
        int creatureGroup = attachEffect.equipmentFirst() ? 1 : 0;
        List<UUID> equipmentTargets = entry.targetsForGroup(equipmentGroup);
        List<UUID> creatureTargets = entry.targetsForGroup(creatureGroup);
        if (equipmentTargets.isEmpty() || creatureTargets.isEmpty()) {
            
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (invalid targets)."));
            log.info("Game {} - Attach equipment ability fizzles, insufficient targets", gameData.id);
            return;
        }

        UUID equipmentId = equipmentTargets.getFirst();
        UUID creatureId = creatureTargets.getFirst();

        Permanent equipment = gameQueryService.findPermanentById(gameData, equipmentId);
        if (equipment == null) {
            
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (equipment no longer on the battlefield)."));
            log.info("Game {} - Attach equipment ability fizzles, equipment left battlefield", gameData.id);
            return;
        }

        Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
        if (creature == null) {
            
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (target creature no longer on the battlefield)."));
            log.info("Game {} - Attach equipment ability fizzles, target creature left battlefield", gameData.id);
            return;
        }

        if (!equipSupport.canAttachEquipment(gameData, equipment, creature)) {
            return;
        }

        UUID oldAttachedTo = equipment.getAttachedTo();

        equipSupport.expireAttachedCopyEffects(gameData, equipment);
        equipment.setAttachedTo(creature.getId());
        // CR 613.7e: an Equipment receives a new timestamp each time it becomes attached.
        equipment.setTimestamp(gameData.nextTimestamp());

        gameLogService.append(gameData, GameLog.cardTextCard(equipment.getCard(), " is now attached to ", creature.getCard(), "."));
        log.info("Game {} - {} attached to {} via {}", gameData.id, equipment.getCard().getName(), creature.getCard().getName(), entry.getCard().getName());

        equipSupport.applySacrificeOnUnattachIfNeeded(gameData, equipment, oldAttachedTo, creature.getId());
        equipSupport.notifyEquipmentAttached(gameData, equipment, oldAttachedTo);
    }
}
