package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachAllEquipmentToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachAllEquipmentToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final EquipSupport equipSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachAllEquipmentToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent host = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (host == null || !gameQueryService.isCreature(gameData, host)) {
            return;
        }

        List<Permanent> equipmentPermanents = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (GameQueryService.permanentHasSubtype(permanent, CardSubtype.EQUIPMENT)) {
                equipmentPermanents.add(permanent);
            }
        });

        for (Permanent equipment : equipmentPermanents) {
            if (host.getId().equals(equipment.getAttachedTo())
                    || !equipSupport.canAttachEquipment(gameData, equipment, host)) {
                continue;
            }

            UUID oldAttachedTo = equipment.getAttachedTo();
            equipSupport.expireAttachedCopyEffects(gameData, equipment);
            equipment.setAttachedTo(host.getId());
            equipment.setTimestamp(gameData.nextTimestamp());
            equipSupport.applySacrificeOnUnattachIfNeeded(gameData, equipment, oldAttachedTo, host.getId());
            equipSupport.notifyEquipmentAttached(gameData, equipment, oldAttachedTo);

            gameLogService.append(gameData,
                    GameLog.cardTextCard(equipment.getCard(), " is now attached to ", host.getCard(), "."));
            log.info("Game {} - {} attached to {}", gameData.id,
                    equipment.getCard().getName(), host.getCard().getName());
        }
    }
}
