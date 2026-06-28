package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnEachOtherCreatureEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutMinusOneMinusOneCounterOnEachOtherCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutMinusOneMinusOneCounterOnEachOtherCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceId = entry.getSourcePermanentId();
        final int[] count = {0};

        gameData.forEachPermanent((playerId, p) -> {
            if (p.getId().equals(sourceId)) return;
            if (!gameQueryService.isCreature(gameData, p)) return;
            if (gameQueryService.cantHaveCounters(gameData, p)) return;
            if (gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, p)) return;

            p.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, p.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + 1);
            count[0]++;
        });

        String logEntry = entry.getCard().getName() + " puts a -1/-1 counter on " + count[0] + " other creature(s).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} puts -1/-1 counter on {} other creature(s)", gameData.id, entry.getCard().getName(), count[0]);
    }
}
