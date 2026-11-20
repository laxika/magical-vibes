package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "TLE", collectorNumber = "239")
@CardRegistration(set = "TLE", collectorNumber = "281")
public class FireNationsConquest extends Card {

    public FireNationsConquest() {
        addStaticEffect(new StaticBoostEffect(1, 0, Subjects.creaturesYouControl()));
    }
}
