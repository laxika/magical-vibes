package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndSacrificeSelfOnLastEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "135")
public class RavagingRiftwurm extends Card {

    public RavagingRiftwurm() {
        addStaticEffect(new KickerEffect("{4}"));
        addStaticEffect(new EnterWithCountersEffect(CounterType.TIME, new Fixed(2)));
        addStaticEffect(new ConditionalEffect(new Kicked(),
                new EnterWithCountersEffect(CounterType.TIME, new Fixed(3))));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new RemoveCounterAndSacrificeSelfOnLastEffect(CounterType.TIME));
    }
}
