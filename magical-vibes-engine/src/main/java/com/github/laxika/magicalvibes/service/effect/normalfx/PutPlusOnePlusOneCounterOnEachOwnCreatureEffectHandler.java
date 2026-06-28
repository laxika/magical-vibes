package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachOwnCreatureEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutPlusOnePlusOneCounterOnEachOwnCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutPlusOnePlusOneCounterOnEachOwnCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutPlusOnePlusOneCounterOnEachOwnCreatureEffect) effect;
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) return;

        int countersPerCreature = e.count();
        int creatureCount = 0;
        for (Permanent p : battlefield) {
            if (!gameQueryService.isCreature(gameData, p)) continue;
            if (gameQueryService.cantHaveCounters(gameData, p)) continue;

            p.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + countersPerCreature);
            creatureCount++;
        }

        String counterText = countersPerCreature == 1 ? "a +1/+1 counter" : countersPerCreature + " +1/+1 counters";
        String logEntry = entry.getCard().getName() + " puts " + counterText + " on " + creatureCount + " creature(s) you control.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} puts {} +1/+1 counter(s) on {} own creature(s)", gameData.id, entry.getCard().getName(), countersPerCreature, creatureCount);
    }
}
