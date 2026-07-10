package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachSourceEquipmentToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final EquipSupport equipSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachSourceEquipmentToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            String logEntry = entry.getCard().getName() + "'s attach ability fizzles (target creature no longer exists).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Attach source equipment fizzles, target creature left battlefield", gameData.id);
            return;
        }

        // Try sourcePermanentId first (ETB path), fall back to card ID lookup (death trigger path)
        Permanent equipment = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (equipment == null) {
            equipment = equipSupport.findEquipmentByCardId(gameData, entry.getCard().getId());
        }
        if (equipment == null) {
            String logEntry = entry.getCard().getName() + "'s attach ability fizzles (equipment no longer on the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Attach source equipment fizzles, equipment left battlefield", gameData.id);
            return;
        }

        gameData.expireFloatingEffectsForUnattachedSource(equipment.getId());
        equipment.setAttachedTo(target.getId());
        // CR 613.7e: an Equipment receives a new timestamp each time it becomes attached.
        equipment.setTimestamp(gameData.nextTimestamp());

        String logEntry = entry.getCard().getName() + " is now attached to " + target.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} attached to {}", gameData.id, entry.getCard().getName(), target.getCard().getName());
    }
}
