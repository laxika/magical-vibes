package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.MakeDefendingPlayerPlayWithHandRevealedEffect;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link MakeDefendingPlayerPlayWithHandRevealedEffect} (Stromgald Spy) into itself as a floating effect on
 * the stack entry's {@code targetId} (the defending player) lasting for as long as the source creature remains on the
 * battlefield ({@link Duration#WHILE_SOURCE_REMAINS}; it ends for good as the source phases out, CR 702.26f), so that
 * player's hand is publicly visible while it lasts.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MakeDefendingPlayerPlayWithHandRevealedEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeDefendingPlayerPlayWithHandRevealedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceId = entry.getSourcePermanentId();
        UUID defenderId = entry.getTargetId();
        // A "for as long as" reveal whose source already left never begins (CR 611.2b).
        if (sourceId == null || defenderId == null || gameQueryService.findPermanentById(gameData, sourceId) == null) {
            return;
        }

        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(), sourceId,
                entry.getControllerId(), effect, defenderId, Duration.WHILE_SOURCE_REMAINS));
        gameLogService.append(gameData,
                GameLog.text(entry.getCard().getName() + " makes the defending player play with their hand revealed."));
        log.info("Game {} - player {} now plays with their hand revealed while {} stays on the battlefield",
                gameData.id, defenderId, sourceId);
    }
}
