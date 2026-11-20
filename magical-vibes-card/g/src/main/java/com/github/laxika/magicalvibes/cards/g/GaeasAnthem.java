package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "PLC", collectorNumber = "147")
@CardRegistration(set = "MM3", collectorNumber = "127")
@CardRegistration(set = "TSR", collectorNumber = "204")
public class GaeasAnthem extends Card {

    public GaeasAnthem() {
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.creaturesYouControl()));
    }
}
