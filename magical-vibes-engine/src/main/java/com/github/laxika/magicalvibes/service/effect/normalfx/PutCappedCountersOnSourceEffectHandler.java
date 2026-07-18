package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCappedCountersOnSourceEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PutCappedCountersOnSourceEffect}: put up to the evaluated amount of counters on
 * the source permanent, clamped so the total of that counter type never exceeds the cap. E.g.
 * Clockwork Beast's "{X}, {T}: Put up to X +1/+0 counters on this creature. This ability can't cause
 * the total number of +1/+0 counters on this creature to be greater than seven."
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutCappedCountersOnSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCappedCountersOnSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCappedCountersOnSourceEffect) effect;
        if (entry.getSourcePermanentId() == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || gameQueryService.cantHaveCounters(gameData, source)) {
            return;
        }

        int requested = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source));
        int current = source.getCounterCount(e.counterType());
        int toAdd = Math.min(requested, e.cap() - current);
        if (toAdd <= 0) {
            return;
        }

        source.setCounterCount(e.counterType(), current + toAdd);
        String logEntry = source.getCard().getName() + " gets " + toAdd + " counter(s).";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(source.getCard()).text(" gets " + toAdd + " counter(s).").build());
        log.info("Game {} - {} gets {} {} counter(s)", gameData.id,
                source.getCard().getName(), toAdd, e.counterType());
    }
}
