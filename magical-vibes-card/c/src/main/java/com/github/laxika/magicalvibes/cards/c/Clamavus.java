package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "40K", collectorNumber = "90")
public class Clamavus extends Card {

    public Clamavus() {
        addStaticEffect(new DynamicStaticBoostEffect(
                new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE),
                new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE),
                Subjects.creaturesYouControl(), null, true));
    }
}
