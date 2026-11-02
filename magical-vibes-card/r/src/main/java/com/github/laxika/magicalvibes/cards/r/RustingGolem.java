package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterOrSacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEM", collectorNumber = "138")
public class RustingGolem extends Card {

    public RustingGolem() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.FADE, new Fixed(5)));

        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new RemoveCounterOrSacrificeSelfEffect(CounterType.FADE));

        DynamicAmount fadeCounters = new CountersOnSource(CounterType.FADE);
        addStaticEffect(new SetPowerToughnessToAmountEffect(fadeCounters, fadeCounters));
    }
}
