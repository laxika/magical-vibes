package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureCantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TargetCreatureCantBlockThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetCreatureCantBlockThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // Multi-target: apply to each valid target
        if (entry.getTargetIds() != null && !entry.getTargetIds().isEmpty()) {
            for (UUID targetId : entry.getTargetIds()) {
                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target == null) {
                    continue;
                }
                target.setCantBlockThisTurn(true);
                String logMsg = target.getCard().getName() + " can't block this turn.";
                gameBroadcastService.logAndBroadcast(gameData, logMsg);
                log.info("Game {} - {} can't block this turn", gameData.id, target.getCard().getName());
            }
            return;
        }

        // Single-target fallback
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        target.setCantBlockThisTurn(true);

        String logEntry = target.getCard().getName() + " can't block this turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} can't block this turn", gameData.id, target.getCard().getName());
    }
}
