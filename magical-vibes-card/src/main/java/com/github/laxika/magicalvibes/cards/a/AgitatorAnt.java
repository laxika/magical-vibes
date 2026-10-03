package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTriggeringCreatureUntilNextTurnEffect;

@CardRegistration(set = "NCC", collectorNumber = "263")
@CardRegistration(set = "MKC", collectorNumber = "145")
@CardRegistration(set = "C20", collectorNumber = "49")
@CardRegistration(set = "NEC", collectorNumber = "102")
public class AgitatorAnt extends Card {

    public AgitatorAnt() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 2,
                        new GoadTriggeringCreatureUntilNextTurnEffect()));
    }
}
