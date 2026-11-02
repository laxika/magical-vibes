package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndSacrificeSelfOnLastEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "83")
public class WaningWurm extends Card {

    public WaningWurm() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.TIME, new Fixed(2)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new RemoveCounterAndSacrificeSelfOnLastEffect(CounterType.TIME));
    }
}
