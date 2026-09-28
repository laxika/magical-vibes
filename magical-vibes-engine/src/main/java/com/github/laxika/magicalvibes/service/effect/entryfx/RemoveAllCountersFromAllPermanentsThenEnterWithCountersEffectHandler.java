package com.github.laxika.magicalvibes.service.effect.entryfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromAllPermanentsThenEnterWithCountersEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EntryReplacementHandlerBean;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RemoveAllCountersFromAllPermanentsThenEnterWithCountersEffectHandler
        implements EntryReplacementHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAllCountersFromAllPermanentsThenEnterWithCountersEffect.class;
    }

    @Override
    public void apply(GameData gameData, UUID controllerId, Permanent enteringPermanent, CardEffect effect) {
        RemoveAllCountersFromAllPermanentsThenEnterWithCountersEffect entryEffect =
                (RemoveAllCountersFromAllPermanentsThenEnterWithCountersEffect) effect;
        int removed = 0;
        for (var battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                int oilRemoved = permanent.getCounterCount(CounterType.OIL);
                int removedFromPermanent = 0;
                for (CounterType counterType : CounterType.values()) {
                    if (counterType == CounterType.ANY || counterType == CounterType.SILVER) {
                        continue;
                    }
                    removedFromPermanent += permanent.getCounterCount(counterType);
                    permanent.setCounterCount(counterType, 0);
                }
                gameData.recordOilCounterRemoved(permanent, oilRemoved);
                removed += removedFromPermanent;
            }
        }

        if (removed <= 0) {
            return;
        }
        int enteringCounters = entryEffect.counterType() == CounterType.PLUS_ONE_PLUS_ONE
                ? gameQueryService.doublePlusOnePlusOneCounters(
                gameData, enteringPermanent, controllerId, removed)
                : gameQueryService.replaceCounters(
                gameData, enteringPermanent, controllerId, entryEffect.counterType(), removed);
        if (enteringCounters > 0) {
            enteringPermanent.setCounterCount(entryEffect.counterType(),
                    enteringPermanent.getCounterCount(entryEffect.counterType()) + enteringCounters);
        }
    }
}
