package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToTriggeringPermanentEffect;
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
public class AttachTargetEquipmentToTriggeringPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final EquipSupport equipSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachTargetEquipmentToTriggeringPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent host = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        List<UUID> targetIds = entry.targetsForEffect((AttachTargetEquipmentToTriggeringPermanentEffect) effect);
        if (targetIds.isEmpty()) {
            return;
        }
        if (host == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s attach ability fizzles (the host is no longer on the battlefield)."));
            return;
        }

        for (UUID targetId : targetIds) {
            Permanent equipment = gameQueryService.findPermanentById(gameData, targetId);
            if (equipment == null || !GameQueryService.permanentHasSubtype(equipment, CardSubtype.EQUIPMENT)
                    || !equipSupport.canAttachEquipment(gameData, equipment, host)) {
                continue;
            }

            var oldAttachedTo = equipment.getAttachedTo();
            equipSupport.expireAttachedCopyEffects(gameData, equipment);
            equipment.setAttachedTo(host.getId());
            equipment.setTimestamp(gameData.nextTimestamp());
            equipSupport.applySacrificeOnUnattachIfNeeded(gameData, equipment, oldAttachedTo, host.getId());
            equipSupport.notifyEquipmentAttached(gameData, equipment, oldAttachedTo);

            gameLogService.append(gameData, GameLog.cardTextCard(equipment.getCard(), " is now attached to ", host.getCard(), "."));
            log.info("Game {} - {} attached to {} via {}", gameData.id, equipment.getCard().getName(),
                    host.getCard().getName(), entry.getCard().getName());
        }
    }
}
