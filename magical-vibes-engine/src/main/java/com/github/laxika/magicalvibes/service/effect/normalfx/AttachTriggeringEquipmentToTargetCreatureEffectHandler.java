package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachTriggeringEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachTriggeringEquipmentToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final EquipSupport equipSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachTriggeringEquipmentToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent equipment = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        Permanent creature = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (equipment == null || !GameQueryService.permanentHasSubtype(equipment, CardSubtype.EQUIPMENT)) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s attach ability fizzles (the Equipment is no longer on the battlefield)."));
            log.info("Game {} - Attach triggering equipment fizzles, equipment left battlefield", gameData.id);
            return;
        }
        if (creature == null || !equipSupport.canAttachEquipment(gameData, equipment, creature)) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s attach ability fizzles (the target creature is no longer legal)."));
            log.info("Game {} - Attach triggering equipment fizzles, target creature is no longer legal", gameData.id);
            return;
        }

        UUID oldAttachedTo = equipment.getAttachedTo();
        gameData.expireFloatingEffectsForUnattachedSource(equipment.getId());
        equipment.setAttachedTo(creature.getId());
        equipment.setTimestamp(gameData.nextTimestamp());
        equipSupport.applySacrificeOnUnattachIfNeeded(gameData, equipment, oldAttachedTo, creature.getId());

        gameLogService.append(gameData, GameLog.cardTextCard(equipment.getCard(), " is now attached to ",
                creature.getCard(), "."));
        log.info("Game {} - {} attached to {} via {}", gameData.id, equipment.getCard().getName(),
                creature.getCard().getName(), entry.getCard().getName());
    }
}
