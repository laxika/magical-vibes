package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "220")
@CardRegistration(set = "MOM", collectorNumber = "384")
public class BotanicalBrawler extends Card {

    public BotanicalBrawler() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2)));
        addTrigger(Triggers.plusOneCountersPutOnAnotherPermanentYouControlFirstTimeEachTurn(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
