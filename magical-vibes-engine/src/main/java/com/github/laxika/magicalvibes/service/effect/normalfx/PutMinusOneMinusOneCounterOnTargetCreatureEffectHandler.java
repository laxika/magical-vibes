package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutMinusOneMinusOneCounterOnTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutMinusOneMinusOneCounterOnTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutMinusOneMinusOneCounterOnTargetCreatureEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            log.info("Game {} - Target creature no longer on battlefield, effect fizzles", gameData.id);
            return;
        }

        if (gameQueryService.cantHaveCounters(gameData, target)) {
            return;
        }

        if (gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, target)) {
            return;
        }

        int count = e.count();
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + count);

        String counterText = count == 1 ? "a -1/-1 counter" : count + " -1/-1 counters";
        String logEntry = target.getCard().getName() + " gets " + counterText + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gets {} -1/-1 counter(s)", gameData.id, target.getCard().getName(), count);

        if (e.regenerateIfSurvives()) {
            int effectiveToughness = gameQueryService.getEffectiveToughness(gameData, target);
            if (effectiveToughness >= 1) {
                target.setRegenerationShield(target.getRegenerationShield() + 1);

                String regenLog = target.getCard().getName() + " gains a regeneration shield.";
                gameBroadcastService.logAndBroadcast(gameData, regenLog);
                log.info("Game {} - {} gains a regeneration shield (toughness {})", gameData.id, target.getCard().getName(), effectiveToughness);
            } else {
                log.info("Game {} - {} has toughness {}, no regeneration shield", gameData.id, target.getCard().getName(), effectiveToughness);
            }
        }
    }
}
