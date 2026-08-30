package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AdjustTimeCountersOnEachSuspendedCardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdjustTimeCountersOnEachSuspendedCardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final RemoveTimeCounterFromExiledCardEffectHandler removeTimeCounterHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AdjustTimeCountersOnEachSuspendedCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        AdjustTimeCountersOnEachSuspendedCardEffect adjustment =
                (AdjustTimeCountersOnEachSuspendedCardEffect) effect;
        List<UUID> suspendedCardIds = new ArrayList<>(gameData.exiledCardTimeCounters.keySet());
        for (UUID cardId : suspendedCardIds) {
            ExiledCardEntry exiledEntry = gameData.findExiledCard(cardId);
            Integer timeCounters = gameData.exiledCardTimeCounters.get(cardId);
            if (exiledEntry == null || exiledEntry.faceDown() || timeCounters == null || timeCounters <= 0) {
                continue;
            }

            if (adjustment.add()) {
                gameData.exiledCardTimeCounters.merge(cardId, adjustment.amount(), Integer::sum);
                gameLogService.append(gameData,
                        GameLog.cardThen(exiledEntry.card(), " gets " + adjustment.amount() + " time counters."));
            } else {
                for (int i = 0; i < adjustment.amount(); i++) {
                    removeTimeCounterHandler.removeTimeCounter(gameData, cardId);
                }
            }
        }
    }
}
