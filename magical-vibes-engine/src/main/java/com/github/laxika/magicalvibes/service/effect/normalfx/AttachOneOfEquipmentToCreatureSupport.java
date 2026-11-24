package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AttachOneOfEquipmentToCreatureSupport {

    private final GameQueryService gameQueryService;
    private final EquipSupport equipSupport;
    private final GameLogService gameLogService;

    public void attach(GameData gameData, UUID equipmentPermanentId, UUID creaturePermanentId) {
        Permanent equipment = gameQueryService.findPermanentById(gameData, equipmentPermanentId);
        Permanent creature = gameQueryService.findPermanentById(gameData, creaturePermanentId);
        if (equipment == null || creature == null || !equipSupport.attachEquipment(gameData, equipment, creature)) {
            return;
        }

        gameLogService.append(gameData,
                GameLog.cardTextCard(equipment.getCard(), " is now attached to ", creature.getCard(), "."));
    }
}
