package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAtNextCleanupEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Flags the source permanent for sacrifice at the beginning of the next cleanup step.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeSelfAtNextCleanupEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeSelfAtNextCleanupEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        if (sourceId == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null) {
            return;
        }

        source.setSacrificeAtNextCleanup(true);
        gameLogService.append(gameData, GameLog.text(source.getCard().getName()
                + " will be sacrificed at the beginning of the next cleanup step."));
        log.info("Game {} - {} scheduled for sacrifice at next cleanup", gameData.id, source.getCard().getName());
    }
}
