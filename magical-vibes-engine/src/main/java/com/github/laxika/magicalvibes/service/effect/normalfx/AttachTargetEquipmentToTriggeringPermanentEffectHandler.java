package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachTargetEquipmentToTriggeringPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final EquipSupport equipSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachTargetEquipmentToTriggeringPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent equipment = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        Permanent host = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        if (equipment == null || !GameQueryService.permanentHasSubtype(equipment, CardSubtype.EQUIPMENT)) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s attach ability fizzles (the Equipment is no longer on the battlefield)."));
            log.info("Game {} - Attach target equipment fizzles, equipment left battlefield", gameData.id);
            return;
        }
        if (host == null || !equipSupport.canAttachEquipment(gameData, equipment, host)) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s attach ability fizzles (the entering permanent is no longer legal)."));
            log.info("Game {} - Attach target equipment fizzles, host is no longer legal", gameData.id);
            return;
        }

        var oldAttachedTo = equipment.getAttachedTo();
        equipSupport.expireAttachedCopyEffects(gameData, equipment);
        equipment.setAttachedTo(host.getId());
        equipment.setTimestamp(gameData.nextTimestamp());
        equipSupport.applySacrificeOnUnattachIfNeeded(gameData, equipment, oldAttachedTo, host.getId());
        equipSupport.notifyEquipmentAttached(gameData, equipment, oldAttachedTo);

        gameLogService.append(gameData, GameLog.cardTextCard(equipment.getCard(), " is now attached to ", host.getCard(), "."));
        log.info("Game {} - {} attached to {} via {}", gameData.id, equipment.getCard().getName(),
                host.getCard().getName(), entry.getCard().getName());
    }
}
