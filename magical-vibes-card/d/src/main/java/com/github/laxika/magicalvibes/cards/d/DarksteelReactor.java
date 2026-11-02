package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAtLeastCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "114")
@CardRegistration(set = "HA1", collectorNumber = "20")
@CardRegistration(set = "EOC", collectorNumber = "134")
public class DarksteelReactor extends Card {

    public DarksteelReactor() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new MayEffect(new PutCountersOnSelfEffect(CounterType.CHARGE),
                        "Put a charge counter on Darksteel Reactor?"));

        addTrigger(Triggers.state(), new StateTriggerEffect(
                new PermanentHasAtLeastCountersPredicate(CounterType.CHARGE, 20),
                List.of(new WinGameEffect()),
                "Darksteel Reactor's state-triggered ability"));
    }
}
