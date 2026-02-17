package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EquipResolutionService implements EffectHandlerProvider {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public void registerHandlers(EffectHandlerRegistry registry) {
        registry.register(EquipEffect.class, (gd, entry, effect) -> resolveEquip(gd, entry));
    }

    private void resolveEquip(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            String logEntry = entry.getCard().getName() + "'s equip ability fizzles (target creature no longer exists).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Equip fizzles, target creature left battlefield", gameData.id);
            return;
        }

        // Find the equipment permanent on the battlefield by matching the card
        Permanent equipment = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (p.getCard().getId().equals(entry.getCard().getId())) {
                    equipment = p;
                    break;
                }
            }
            if (equipment != null) break;
        }

        if (equipment == null) {
            String logEntry = entry.getCard().getName() + "'s equip ability fizzles (equipment no longer on the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Equip fizzles, equipment left battlefield", gameData.id);
            return;
        }

        equipment.setAttachedTo(target.getId());

        String logEntry = entry.getCard().getName() + " is now attached to " + target.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} equipped to {}", gameData.id, entry.getCard().getName(), target.getCard().getName());
    }
}
