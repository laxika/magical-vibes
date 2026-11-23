package com.github.laxika.magicalvibes.service.effect.entryfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromAllPermanentsThenEnterWithCountersEffect;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EntryReplacementHandlerBean;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Thief of Blood's "as this enters, remove all counters from all permanents; it enters with a +1/+1 counter for each
 * counter removed this way". Each permanent's removal is followed by the checks every removal of all counters makes
 * (as {@code RemoveAllCountersFromTargetPermanentEffectHandler} does): vanishing's last-time-counter sacrifice
 * (CR 702.63a) and a battle's defeat.
 */
@Component
public class RemoveAllCountersFromAllPermanentsThenEnterWithCountersEffectHandler
        implements EntryReplacementHandlerBean {

    @Autowired
    private GameQueryService gameQueryService;

    @Autowired
    @Lazy
    private PermanentCounterSupport permanentCounterSupport;

    @Autowired
    @Lazy
    private BattleDefeatSupport battleDefeatSupport;

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
                int timeCountersBefore = permanent.getCounterCount(CounterType.TIME);
                boolean hadDefenseCounters = permanent.getCounterCount(CounterType.DEFENSE) > 0;
                int removedFromPermanent = 0;
                for (CounterType counterType : CounterType.values()) {
                    if (counterType == CounterType.ANY || counterType == CounterType.SILVER) {
                        continue;
                    }
                    removedFromPermanent += permanent.getCounterCount(counterType);
                    permanent.setCounterCount(counterType, 0);
                }
                gameData.recordOilCounterRemoved(permanent, oilRemoved);
                permanentCounterSupport.checkLastTimeCounterRemoved(gameData, permanent, CounterType.TIME,
                        timeCountersBefore);
                if (hadDefenseCounters) {
                    battleDefeatSupport.checkAfterDefenseRemoved(gameData, permanent);
                }
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
