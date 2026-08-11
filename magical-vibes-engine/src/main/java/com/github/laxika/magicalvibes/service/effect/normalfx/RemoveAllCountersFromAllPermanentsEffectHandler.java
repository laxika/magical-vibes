package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromAllPermanentsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RemoveAllCountersFromAllPermanentsEffect}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAllCountersFromAllPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAllCountersFromAllPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int affected = 0;
        int removed = 0;
        for (var battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                int removedFromPermanent = 0;
                for (CounterType counterType : CounterType.values()) {
                    if (counterType == CounterType.ANY || counterType == CounterType.SILVER) {
                        continue;
                    }
                    removedFromPermanent += permanent.getCounterCount(counterType);
                    permanent.setCounterCount(counterType, 0);
                }
                if (removedFromPermanent > 0) {
                    affected++;
                    removed += removedFromPermanent;
                }
            }
        }

        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text(" removes all counters from all permanents (" + removed + " counter(s) from "
                        + affected + " permanent(s)).")
                .build());
        log.info("Game {} - {} removes {} counter(s) from {} permanent(s)",
                gameData.id, entry.getCard().getName(), removed, affected);
    }
}
