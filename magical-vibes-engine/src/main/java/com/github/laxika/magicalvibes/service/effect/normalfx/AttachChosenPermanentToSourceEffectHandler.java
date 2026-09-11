package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachChosenPermanentToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachChosenPermanentToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final EquipSupport equipSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachChosenPermanentToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent equipment = gameQueryService.findPermanentById(gameData, entry.getChosenPermanentId());
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (equipment == null || source == null
                || !GameQueryService.permanentHasSubtype(equipment, CardSubtype.EQUIPMENT)
                || !equipSupport.canAttachEquipment(gameData, equipment, source)) {
            return;
        }

        equipSupport.attachEquipment(gameData, equipment, source);
        gameLogService.append(gameData,
                GameLog.cardTextCard(equipment.getCard(), " is attached to ", source.getCard(), "."));
        log.info("Game {} - {} attached to {}", gameData.id,
                equipment.getCard().getName(), source.getCard().getName());
    }
}
