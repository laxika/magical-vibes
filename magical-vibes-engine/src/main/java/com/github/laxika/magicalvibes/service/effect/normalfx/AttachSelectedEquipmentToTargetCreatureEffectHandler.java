package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachSelectedEquipmentToTargetCreatureEffect;
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
public class AttachSelectedEquipmentToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final EquipSupport equipSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachSelectedEquipmentToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var attachEffect = (AttachSelectedEquipmentToTargetCreatureEffect) effect;
        Permanent equipment = gameQueryService.findPermanentById(gameData, attachEffect.equipmentPermanentId());
        Permanent creature = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (equipment == null || creature == null
                || !gameQueryService.isCreature(gameData, creature)
                || !entry.getControllerId().equals(
                gameQueryService.findPermanentController(gameData, creature.getId()))) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s attach ability fizzles (the target creature is no longer legal)."));
            log.info("Game {} - Selected equipment attach fizzles, target creature is no longer legal", gameData.id);
            return;
        }

        if (!equipSupport.canAttachEquipment(gameData, equipment, creature)) {
            return;
        }

        UUID oldAttachedTo = equipment.getAttachedTo();
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
