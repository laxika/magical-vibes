package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

@CardRegistration(set = "SNC", collectorNumber = "182")
public class DisciplinedDuelist extends Card {

    public DisciplinedDuelist() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.SHIELD, new Fixed(1)));
    }
}
