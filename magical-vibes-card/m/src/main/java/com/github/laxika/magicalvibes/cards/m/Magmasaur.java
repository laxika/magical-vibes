package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MagmasaurUpkeepEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMP", collectorNumber = "188")
@CardRegistration(set = "TPR", collectorNumber = "141")
public class Magmasaur extends Card {

    public Magmasaur() {
        // This creature enters with five +1/+1 counters on it.
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(5)));

        // At the beginning of your upkeep, you may remove a +1/+1 counter from this creature. If you
        // don't, sacrifice this creature and it deals damage equal to the number of +1/+1 counters
        // on it to each creature without flying and each player.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MagmasaurUpkeepEffect());
    }
}
