package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeDefendingPlayerPlayWithHandRevealedEffect;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link MakeDefendingPlayerPlayWithHandRevealedEffect} (Stromgald Spy): records the stack
 * entry's {@code targetId} (the defending player) under its {@code sourcePermanentId} in
 * {@code GameData.handsRevealedWhileSourceOnBattlefield}, so that player's hand is publicly visible
 * for as long as the source creature remains on the battlefield.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MakeDefendingPlayerPlayWithHandRevealedEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeDefendingPlayerPlayWithHandRevealedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceId = entry.getSourcePermanentId();
        UUID defenderId = entry.getTargetId();
        if (sourceId == null || defenderId == null) {
            return;
        }

        gameData.handsRevealedWhileSourceOnBattlefield
                .computeIfAbsent(sourceId, id -> ConcurrentHashMap.newKeySet())
                .add(defenderId);
        gameLogService.append(gameData,
                GameLog.text(entry.getCard().getName() + " makes the defending player play with their hand revealed."));
        log.info("Game {} - player {} now plays with their hand revealed while {} stays on the battlefield",
                gameData.id, defenderId, sourceId);
    }
}
