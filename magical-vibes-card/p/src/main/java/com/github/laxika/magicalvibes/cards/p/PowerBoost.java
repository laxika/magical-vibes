package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MSC", collectorNumber = "852")
public class PowerBoost extends Card {

    public PowerBoost() {
        addStaticEffect(new StaticBoostEffect(1, 0, Subjects.creaturesYouControl()));
    }
}
