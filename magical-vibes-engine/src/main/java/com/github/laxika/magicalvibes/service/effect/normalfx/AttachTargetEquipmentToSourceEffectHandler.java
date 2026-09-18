package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AttachTargetEquipmentToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final EquipSupport equipSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachTargetEquipmentToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent equipment = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (source == null || equipment == null
                || !gameQueryService.isCreature(gameData, source)
                || !GameQueryService.permanentHasSubtype(equipment, CardSubtype.EQUIPMENT)
                || !entry.getControllerId().equals(gameData.findControllerOf(equipment))
                || !equipSupport.canAttachEquipment(gameData, equipment, source)) {
            return;
        }

        UUID oldAttachedTo = equipment.getAttachedTo();
        equipSupport.expireAttachedCopyEffects(gameData, equipment);
        equipment.setAttachedTo(source.getId());
        equipment.setTimestamp(gameData.nextTimestamp());
        equipSupport.applySacrificeOnUnattachIfNeeded(gameData, equipment, oldAttachedTo, source.getId());
        equipSupport.notifyEquipmentAttached(gameData, equipment, oldAttachedTo);
        gameLogService.append(gameData,
                GameLog.cardTextCard(equipment.getCard(), " is now attached to ", source.getCard(), "."));
    }
}
