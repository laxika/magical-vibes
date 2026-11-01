package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

@CardRegistration(set = "FDN", collectorNumber = "87")
public class GoblinBoarders extends Card {

    public GoblinBoarders() {
        addStaticEffect(new ConditionalEffect(new Raid(),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1))));
    }
}
