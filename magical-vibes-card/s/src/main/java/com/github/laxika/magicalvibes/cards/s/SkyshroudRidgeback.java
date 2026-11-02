package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterOrSacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEM", collectorNumber = "120")
public class SkyshroudRidgeback extends Card {

    public SkyshroudRidgeback() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.FADE, new Fixed(2)));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new RemoveCounterOrSacrificeSelfEffect(CounterType.FADE));
    }
}
