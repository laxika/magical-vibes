package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.UnattachEquipmentIfAttachedToControlledCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UnattachEquipmentIfAttachedToControlledCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final EquipSupport equipSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UnattachEquipmentIfAttachedToControlledCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var unattachEffect = (UnattachEquipmentIfAttachedToControlledCreatureEffect) effect;
        Permanent equipment = gameQueryService.findPermanentById(gameData, unattachEffect.equipmentId());
        if (equipment == null || equipment.getAttachedTo() == null) {
            return;
        }

        Permanent creature = gameQueryService.findPermanentById(gameData, equipment.getAttachedTo());
        if (creature == null || !gameQueryService.isCreature(gameData, creature)
                || !Objects.equals(entry.getControllerId(), gameQueryService.findPermanentController(gameData, creature.getId()))) {
            return;
        }

        var oldAttachedTo = equipment.getAttachedTo();
        equipment.setAttachedTo(null);
        gameData.expireFloatingEffectsForUnattachedSource(equipment.getId());
        equipSupport.expireAttachedCopyEffects(gameData, equipment);
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" unattaches ")
                .card(equipment.getCard())
                .text(" from ")
                .card(creature.getCard())
                .build());
        equipSupport.applySacrificeOnUnattachIfNeeded(gameData, equipment, oldAttachedTo, null);
    }
}
