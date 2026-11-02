package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PayXManaPutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BNG", collectorNumber = "123")
@CardRegistration(set = "UMA", collectorNumber = "169")
@CardRegistration(set = "PIO", collectorNumber = "178")
public class HeroOfLeinaTower extends Card {

    public HeroOfLeinaTower() {
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                null,
                List.of(new PayXManaPutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                new StackEntryTargetsSourcePredicate()
        ));
    }
}
