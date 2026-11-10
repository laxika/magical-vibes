package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DrawGameEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LEG", collectorNumber = "8")
@CardRegistration(set = "ME3", collectorNumber = "8")
public class DivineIntervention extends Card {

    public DivineIntervention() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.INTERVENTION, new Fixed(2)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new RemoveCounterFromSourceEffect(CounterType.INTERVENTION, 1));
        addTrigger(Triggers.youRemoveLastCounterFromThis(CounterType.INTERVENTION), new DrawGameEffect());
    }
}
