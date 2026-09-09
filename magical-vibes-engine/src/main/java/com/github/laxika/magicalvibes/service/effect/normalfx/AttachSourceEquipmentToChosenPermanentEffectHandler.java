package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttachSourceEquipmentToChosenPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final EquipSupport equipSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachSourceEquipmentToChosenPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getChosenPermanentId() == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, entry.getChosenPermanentId());
        if (target == null) {
            return;
        }

        Permanent equipment = entry.getSourcePermanentId() == null
                ? equipSupport.findEquipmentByCardId(gameData, entry.getCard().getId())
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (equipment == null || !equipSupport.attachEquipment(gameData, equipment, target)) {
            return;
        }

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " is attached to the manifested creature."));
    }
}
