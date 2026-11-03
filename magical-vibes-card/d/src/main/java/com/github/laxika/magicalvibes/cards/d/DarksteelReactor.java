package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAtLeastCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DST", collectorNumber = "114")
@CardRegistration(set = "HA1", collectorNumber = "20")
@CardRegistration(set = "EOC", collectorNumber = "134")
public class DarksteelReactor extends Card {

    public DarksteelReactor() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new MayEffect(new PutCountersOnSelfEffect(CounterType.CHARGE),
                        "Put a charge counter on Darksteel Reactor?"));

        addTrigger(Triggers.sourceState(new PermanentHasAtLeastCountersPredicate(CounterType.CHARGE, 20)),
                new WinGameEffect());
    }
}
