package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

@CardRegistration(set = "ROE", collectorNumber = "49")
public class TimeOfHeroes extends Card {

    public TimeOfHeroes() {
        addStaticEffect(new StaticBoostEffect(2, 2, Subjects.creaturesYouControl(),
                new PermanentHasCountersPredicate(CounterType.LEVEL)));
    }
}
