package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachCreatedEquipmentToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Attaches Equipment conjured earlier by the same resolving ETB ability to its source creature. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttachCreatedEquipmentToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final EquipSupport equipSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachCreatedEquipmentToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent host = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (host == null || !gameQueryService.isCreature(gameData, host)) {
            return;
        }

        List<Permanent> createdPermanents = new ArrayList<>();
        for (UUID createdId : entry.getCreatedPermanentIds()) {
            Permanent created = gameQueryService.findPermanentById(gameData, createdId);
            if (created != null && GameQueryService.permanentHasSubtype(created, CardSubtype.EQUIPMENT)) {
                createdPermanents.add(created);
            }
        }

        for (Permanent equipment : createdPermanents) {
            if (host.getId().equals(equipment.getAttachedTo())
                    || !equipSupport.canAttachEquipment(gameData, equipment, host)) {
                continue;
            }

            if (!equipSupport.attachEquipment(gameData, equipment, host)) {
                continue;
            }

            gameLogService.append(gameData,
                    GameLog.cardTextCard(equipment.getCard(), " is now attached to ", host.getCard(), "."));
            log.info("Game {} - {} attached to {}", gameData.id,
                    equipment.getCard().getName(), host.getCard().getName());
        }
    }
}
