package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachTargetEquipmentToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final EquipSupport equipSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachTargetEquipmentToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targets = entry.getTargetIds();
        if (targets == null || targets.size() < 2) {
            String logEntry = entry.getCard().getName() + "'s ability fizzles (invalid targets).";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (invalid targets)."));
            log.info("Game {} - Attach equipment ability fizzles, insufficient targets", gameData.id);
            return;
        }

        UUID equipmentId = targets.get(0);
        UUID creatureId = targets.get(1);

        Permanent equipment = gameQueryService.findPermanentById(gameData, equipmentId);
        if (equipment == null) {
            String logEntry = entry.getCard().getName() + "'s ability fizzles (equipment no longer on the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (equipment no longer on the battlefield)."));
            log.info("Game {} - Attach equipment ability fizzles, equipment left battlefield", gameData.id);
            return;
        }

        Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
        if (creature == null) {
            String logEntry = entry.getCard().getName() + "'s ability fizzles (target creature no longer on the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (target creature no longer on the battlefield)."));
            log.info("Game {} - Attach equipment ability fizzles, target creature left battlefield", gameData.id);
            return;
        }

        UUID oldAttachedTo = equipment.getAttachedTo();

        gameData.expireFloatingEffectsForUnattachedSource(equipment.getId());
        equipment.setAttachedTo(creature.getId());
        // CR 613.7e: an Equipment receives a new timestamp each time it becomes attached.
        equipment.setTimestamp(gameData.nextTimestamp());

        String logEntry = equipment.getCard().getName() + " is now attached to " + creature.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (equipment no longer on the battlefield)."));
        log.info("Game {} - {} attached to {} via {}", gameData.id, equipment.getCard().getName(), creature.getCard().getName(), entry.getCard().getName());

        equipSupport.applySacrificeOnUnattachIfNeeded(gameData, equipment, oldAttachedTo, creature.getId());
    }
}
