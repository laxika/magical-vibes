package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachCreatedEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachCreatedEquipmentToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final EquipSupport equipSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachCreatedEquipmentToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (!(effect instanceof AttachCreatedEquipmentToTargetCreatureEffect)) {
            return;
        }
        UUID equipmentId = entry.getSourcePermanentId();
        Permanent equipment = equipmentId == null
                ? null
                : gameQueryService.findPermanentById(gameData, equipmentId);
        Permanent creature = entry.getTargetId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (equipment == null || creature == null
                || !gameQueryService.isCreature(gameData, creature)
                || !entry.getControllerId().equals(gameQueryService.findPermanentController(gameData, creature.getId()))) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s attach ability fizzles (the target creature is no longer legal)."));
            log.info("Game {} - Created equipment attach fizzles, target creature is no longer legal", gameData.id);
            return;
        }

        if (!equipSupport.canAttachEquipment(gameData, equipment, creature)) {
            return;
        }

        var oldAttachedTo = equipment.getAttachedTo();
        gameData.expireFloatingEffectsForUnattachedSource(equipment.getId());
        equipSupport.expireAttachedCopyEffects(gameData, equipment);
        equipment.setAttachedTo(creature.getId());
        equipment.setTimestamp(gameData.nextTimestamp());
        equipSupport.applySacrificeOnUnattachIfNeeded(gameData, equipment, oldAttachedTo, creature.getId());
        equipSupport.notifyEquipmentAttached(gameData, equipment, oldAttachedTo);

        gameLogService.append(gameData,
                GameLog.cardTextCard(equipment.getCard(), " is now attached to ", creature.getCard(), "."));
        log.info("Game {} - {} attached to {} via {}", gameData.id,
                equipment.getCard().getName(), creature.getCard().getName(), entry.getCard().getName());
    }
}
