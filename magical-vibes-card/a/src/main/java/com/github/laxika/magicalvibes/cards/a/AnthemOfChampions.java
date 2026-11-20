package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "FDN", collectorNumber = "116")
@CardRegistration(set = "FDN", collectorNumber = "344")
@CardRegistration(set = "FDN", collectorNumber = "405")
@CardRegistration(set = "FDN", collectorNumber = "477")
@CardRegistration(set = "MAR", collectorNumber = "85")
public class AnthemOfChampions extends Card {

    public AnthemOfChampions() {
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.creaturesYouControl()));
    }
}
