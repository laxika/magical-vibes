package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTimeCountersOnSuspendedCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutTimeCountersOnSuspendedCardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTimeCountersOnSuspendedCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getEventValue() <= 0) {
            return;
        }

        UUID cardId = entry.getCard().getId();
        ExiledCardEntry exiledEntry = gameData.findExiledCard(cardId);
        if (exiledEntry == null || !gameData.exiledCardTimeCounters.containsKey(cardId)) {
            return;
        }

        int amount = ((PutTimeCountersOnSuspendedCardEffect) effect).amount();
        gameData.exiledCardTimeCounters.merge(cardId, amount, Integer::sum);
        gameLogService.append(gameData,
                GameLog.cardThen(exiledEntry.card(), " gets " + amount + " time counters."));
        log.info("Game {} - {} gets {} time counters", gameData.id, exiledEntry.card().getName(), amount);
    }
}
