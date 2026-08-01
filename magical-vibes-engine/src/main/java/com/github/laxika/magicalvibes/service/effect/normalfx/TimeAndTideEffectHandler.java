package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TimeAndTideEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.turn.PhasingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link TimeAndTideEffect}: simultaneously phases in every phased-out creature and
 * phases out every creature with phasing, via {@link PhasingService#applyTimeAndTide}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TimeAndTideEffectHandler implements NormalEffectHandlerBean {

    private final PhasingService phasingService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TimeAndTideEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        phasingService.applyTimeAndTide(gameData);
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" simultaneously phases in all phased-out creatures and phases out all creatures with phasing.")
                .build());
        log.info("Game {} - {} resolves Time and Tide", gameData.id,
                entry.getCard() != null ? entry.getCard().getName() : "effect");
    }
}
