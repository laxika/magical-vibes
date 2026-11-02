package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterOrSacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEM", collectorNumber = "102")
@CardRegistration(set = "DDD", collectorNumber = "7")
@CardRegistration(set = "VMA", collectorNumber = "198")
@CardRegistration(set = "GVL", collectorNumber = "7")
public class Blastoderm extends Card {

    public Blastoderm() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.FADE, new Fixed(3)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new RemoveCounterOrSacrificeSelfEffect(CounterType.FADE));
    }
}
