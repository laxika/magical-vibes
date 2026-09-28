package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutCountersOnCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTriggeringCreatureUntilNextTurnEffect;

@CardRegistration(set = "NCC", collectorNumber = "263")
@CardRegistration(set = "C20", collectorNumber = "49")
public class AgitatorAnt extends Card {

    public AgitatorAnt() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new EachPlayerMayPutCountersOnCreatureEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 2,
                        new GoadTriggeringCreatureUntilNextTurnEffect()));
    }
}
