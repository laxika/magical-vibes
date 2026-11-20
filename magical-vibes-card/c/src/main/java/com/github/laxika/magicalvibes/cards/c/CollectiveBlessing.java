package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "RTR", collectorNumber = "150")
@CardRegistration(set = "SCD", collectorNumber = "222")
public class CollectiveBlessing extends Card {

    public CollectiveBlessing() {
        addStaticEffect(new StaticBoostEffect(3, 3, Subjects.creaturesYouControl()));
    }
}
