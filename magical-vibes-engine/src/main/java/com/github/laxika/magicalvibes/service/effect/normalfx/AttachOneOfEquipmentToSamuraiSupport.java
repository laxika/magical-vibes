package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AttachOneOfEquipmentToSamuraiSupport {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final EquipSupport equipSupport;
    private final GameLogService gameLogService;

    public List<UUID> legalSamuraiIds(GameData gameData, UUID controllerId,
                                      List<UUID> equipmentPermanentIds) {
        List<UUID> legalSamuraiIds = new ArrayList<>();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceControllerId(controllerId);
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
            if (!gameQueryService.isCreature(gameData, permanent)
                    || !predicateEvaluationService.matchesPermanentPredicate(
                    permanent, new PermanentHasSubtypePredicate(CardSubtype.SAMURAI), filterContext)) {
                continue;
            }
            if (!legalEquipmentIds(gameData, permanent, equipmentPermanentIds).isEmpty()) {
                legalSamuraiIds.add(permanent.getId());
            }
        }
        return legalSamuraiIds;
    }

    public List<UUID> legalEquipmentIds(GameData gameData, Permanent samurai,
                                        List<UUID> equipmentPermanentIds) {
        List<UUID> legalEquipmentIds = new ArrayList<>();
        for (UUID equipmentPermanentId : equipmentPermanentIds) {
            Permanent equipment = gameQueryService.findPermanentById(gameData, equipmentPermanentId);
            if (equipment != null && GameQueryService.permanentHasSubtype(equipment, CardSubtype.EQUIPMENT)
                    && equipSupport.canAttachEquipment(gameData, equipment, samurai)) {
                legalEquipmentIds.add(equipmentPermanentId);
            }
        }
        return legalEquipmentIds;
    }

    public void attach(GameData gameData, UUID equipmentPermanentId, UUID samuraiPermanentId) {
        Permanent equipment = gameQueryService.findPermanentById(gameData, equipmentPermanentId);
        Permanent samurai = gameQueryService.findPermanentById(gameData, samuraiPermanentId);
        if (equipment == null || samurai == null
                || !GameQueryService.permanentHasSubtype(equipment, CardSubtype.EQUIPMENT)
                || !equipSupport.canAttachEquipment(gameData, equipment, samurai)) {
            return;
        }

        UUID oldAttachedTo = equipment.getAttachedTo();
        gameData.expireFloatingEffectsForUnattachedSource(equipment.getId());
        equipSupport.expireAttachedCopyEffects(gameData, equipment);
        equipment.setAttachedTo(samurai.getId());
        equipment.setTimestamp(gameData.nextTimestamp());
        equipSupport.applySacrificeOnUnattachIfNeeded(gameData, equipment, oldAttachedTo, samurai.getId());
        equipSupport.notifyEquipmentAttached(gameData, equipment, oldAttachedTo);
        gameLogService.append(gameData,
                GameLog.cardTextCard(equipment.getCard(), " is now attached to ", samurai.getCard(), "."));
    }
}
