package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AttachOneOfEquipmentToCreatureSupport {

    private final GameQueryService gameQueryService;
    private final EquipSupport equipSupport;
    private final GameLogService gameLogService;

    public List<UUID> legalCreatureIds(GameData gameData, UUID controllerId,
                                       List<UUID> equipmentPermanentIds) {
        List<UUID> legalCreatureIds = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
            if (gameQueryService.isCreature(gameData, permanent)
                    && !legalEquipmentIds(gameData, controllerId, permanent, equipmentPermanentIds).isEmpty()) {
                legalCreatureIds.add(permanent.getId());
            }
        }
        return legalCreatureIds;
    }

    public List<UUID> legalEquipmentIds(GameData gameData, UUID controllerId,
                                        Permanent creature, List<UUID> equipmentPermanentIds) {
        List<UUID> legalEquipmentIds = new ArrayList<>();
        for (UUID equipmentPermanentId : equipmentPermanentIds) {
            Permanent equipment = gameQueryService.findPermanentById(gameData, equipmentPermanentId);
            if (equipment != null
                    && controllerId.equals(gameQueryService.findPermanentController(gameData, equipmentPermanentId))
                    && GameQueryService.permanentHasSubtype(equipment, CardSubtype.EQUIPMENT)
                    && equipSupport.canAttachEquipment(gameData, equipment, creature)) {
                legalEquipmentIds.add(equipmentPermanentId);
            }
        }
        return legalEquipmentIds;
    }

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
