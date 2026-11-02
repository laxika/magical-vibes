package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTriggeringCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "263")
@CardRegistration(set = "MKC", collectorNumber = "145")
@CardRegistration(set = "C20", collectorNumber = "49")
@CardRegistration(set = "NEC", collectorNumber = "102")
public class AgitatorAnt extends Card {

    public AgitatorAnt() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 2,
                        new GoadTriggeringCreatureUntilNextTurnEffect()));
    }
}
