package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToCreatedPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachTargetEquipmentToCreatedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final EquipSupport equipSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachTargetEquipmentToCreatedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getCreatedPermanentIds().isEmpty()) {
            return;
        }

        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty()) {
            return;
        }

        Permanent equipment = gameQueryService.findPermanentById(gameData, targetIds.getFirst());
        Permanent host = gameQueryService.findPermanentById(gameData, entry.getCreatedPermanentIds().getFirst());
        if (equipment == null || !gameQueryService.hasEffectiveSubtype(gameData, equipment, CardSubtype.EQUIPMENT)) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s ability fizzles (the Equipment is no longer on the battlefield)."));
            return;
        }
        if (host == null || !gameQueryService.isCreature(gameData, host)) {
            return;
        }

        UUID oldAttachedTo = equipment.getAttachedTo();
        if (!equipSupport.canAttachEquipment(gameData, equipment, host)) {
            return;
        }

        gameData.expireFloatingEffectsForUnattachedSource(equipment.getId());
        equipSupport.expireAttachedCopyEffects(gameData, equipment);
        equipment.setAttachedTo(host.getId());
        equipment.setTimestamp(gameData.nextTimestamp());
        equipSupport.applySacrificeOnUnattachIfNeeded(gameData, equipment, oldAttachedTo, host.getId());
        equipSupport.notifyEquipmentAttached(gameData, equipment, oldAttachedTo);

        gameLogService.append(gameData,
                GameLog.cardTextCard(equipment.getCard(), " is now attached to ", host.getCard(), "."));
        log.info("Game {} - {} attached to {} via {}", gameData.id,
                equipment.getCard().getName(), host.getCard().getName(), entry.getCard().getName());
    }
}
