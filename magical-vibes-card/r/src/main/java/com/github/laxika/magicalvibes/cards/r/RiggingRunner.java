package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "XLN", collectorNumber = "157")
public class RiggingRunner extends Card {

    public RiggingRunner() {
        // First strike is loaded from Scryfall.
        // Raid — This creature enters with a +1/+1 counter on it if you attacked this turn.
        addStaticEffect(new ConditionalEffect(new Raid(),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1))));
    }
}
