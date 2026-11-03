package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "182")
@CardRegistration(set = "MH2", collectorNumber = "277")
public class ChanceEncounter extends Card {

    public ChanceEncounter() {
        addTrigger(Triggers.youWinCoinFlip(),
                new PutCountersOnSelfEffect(CounterType.LUCK));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(10, CounterType.LUCK)),
                new WinGameEffect());
    }
}
