package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutMinusOneMinusOneCounterOnEnchantedCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutMinusOneMinusOneCounterOnEnchantedCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutMinusOneMinusOneCounterOnEnchantedCreatureEffect) effect;
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null || !aura.isAttached()) return;

        Permanent creature = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (creature == null) return;

        if (gameQueryService.cantHaveCounters(gameData, creature)) {
            return;
        }

        if (gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, creature)) {
            return;
        }

        int count = e.count();
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + count);

        String counterText = count == 1 ? "a -1/-1 counter" : count + " -1/-1 counters";
        String logEntry = creature.getCard().getName() + " gets " + counterText + " from " + entry.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gets {} -1/-1 counter(s) from {}", gameData.id, creature.getCard().getName(), count, entry.getCard().getName());
    }
}
