package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EquipEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final EquipSupport equipSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EquipEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s equip ability fizzles (target creature no longer exists)."));
            log.info("Game {} - Equip fizzles, target creature left battlefield", gameData.id);
            return;
        }

        Permanent equipment = equipSupport.findEquipmentByCardId(gameData, entry.getCard().getId());

        if (equipment == null) {
            
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s equip ability fizzles (equipment no longer on the battlefield)."));
            log.info("Game {} - Equip fizzles, equipment left battlefield", gameData.id);
            return;
        }

        // Ruling (Haunted Plate Mail): equip while not an Equipment has no effect.
        if (!GameQueryService.permanentHasSubtype(equipment, CardSubtype.EQUIPMENT)) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s equip ability has no effect (it is not an Equipment)."));
            log.info("Game {} - Equip has no effect, {} is not Equipment", gameData.id, entry.getCard().getName());
            return;
        }

        if (!equipSupport.canAttachEquipment(gameData, equipment, target)) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s equip ability has no effect (the target can't be equipped)."));
            log.info("Game {} - Equip has no effect, target cannot be equipped", gameData.id);
            return;
        }

        UUID oldAttachedTo = equipment.getAttachedTo();

        gameData.expireFloatingEffectsForUnattachedSource(equipment.getId());
        equipment.setAttachedTo(target.getId());
        // CR 613.7e: an Equipment receives a new timestamp each time it becomes attached.
        equipment.setTimestamp(gameData.nextTimestamp());

        
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s equip ability fizzles (target creature no longer exists)."));
        log.info("Game {} - {} equipped to {}", gameData.id, entry.getCard().getName(), target.getCard().getName());

        equipSupport.applySacrificeOnUnattachIfNeeded(gameData, equipment, oldAttachedTo, target.getId());
    }
}
