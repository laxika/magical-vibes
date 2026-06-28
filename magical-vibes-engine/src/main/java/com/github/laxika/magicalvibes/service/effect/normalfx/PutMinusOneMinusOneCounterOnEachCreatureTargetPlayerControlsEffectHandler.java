package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnEachCreatureTargetPlayerControlsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutMinusOneMinusOneCounterOnEachCreatureTargetPlayerControlsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutMinusOneMinusOneCounterOnEachCreatureTargetPlayerControlsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) return;

        int count = 0;
        for (Permanent p : battlefield) {
            if (!gameQueryService.isCreature(gameData, p)) continue;
            if (gameQueryService.cantHaveCounters(gameData, p)) continue;
            if (gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, p)) continue;

            p.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, p.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + 1);
            count++;
        }

        String logEntry = entry.getCard().getName() + " puts a -1/-1 counter on " + count + " creature(s) target player controls.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} puts -1/-1 counter on {} creature(s) target player controls", gameData.id, entry.getCard().getName(), count);
    }
}
