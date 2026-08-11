package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves renown (CR 702.112a): if the source creature isn't renowned yet, it gets N +1/+1
 * counters and becomes renowned. An already renowned creature does nothing (CR 702.112c), and
 * the creature becomes renowned even if the counters can't be placed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RenownEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RenownEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RenownEffect) effect;
        if (entry.getSourcePermanentId() == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || source.isRenowned()) {
            return;
        }

        if (!gameQueryService.cantHavePlusOnePlusOneCounters(gameData, source)) {
            int amount = gameQueryService.doublePlusOnePlusOneCounters(gameData, source, e.amount());
            if (amount > 0) {
                source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                        source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + amount);
                gameLogService.append(gameData, GameLog.builder().card(source.getCard())
                        .text(" gets " + amount + " +1/+1 counter(s) and becomes renowned.").build());
                log.info("Game {} - {} becomes renowned with {} +1/+1 counter(s)", gameData.id,
                        source.getCard().getName(), amount);
            }
        }
        source.setRenowned(true);

        UUID controllerId = gameQueryService.findPermanentController(gameData, source.getId());
        if (controllerId != null) {
            triggerCollectionService.checkBecomesRenownedTriggers(gameData, source, controllerId);
        }
    }
}
