package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.UnattachEquipmentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UnattachEquipmentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final EquipSupport equipSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UnattachEquipmentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UnattachEquipmentEffect unattachEffect = (UnattachEquipmentEffect) effect;
        Permanent equipment = gameQueryService.findPermanentById(gameData, unattachEffect.equipmentId());
        if (equipment == null || equipment.getAttachedTo() == null) {
            return;
        }

        UUID oldAttachedTo = equipment.getAttachedTo();
        Permanent creature = gameQueryService.findPermanentById(gameData, oldAttachedTo);
        equipment.setAttachedTo(null);
        gameData.expireFloatingEffectsForUnattachedSource(equipment.getId());
        equipSupport.expireAttachedCopyEffects(gameData, equipment);
        if (creature != null) {
            gameLogService.append(gameData, GameLog.builder()
                    .card(entry.getCard())
                    .text(" unattaches ")
                    .card(equipment.getCard())
                    .text(" from ")
                    .card(creature.getCard())
                    .build());
        }
        equipSupport.applySacrificeOnUnattachIfNeeded(gameData, equipment, oldAttachedTo, null);
    }
}
