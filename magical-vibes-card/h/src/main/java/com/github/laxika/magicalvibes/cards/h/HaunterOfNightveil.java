package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "DGM", collectorNumber = "76")
public class HaunterOfNightveil extends Card {

    public HaunterOfNightveil() {
        addStaticEffect(new StaticBoostEffect(-1, 0, Subjects.creaturesYourOpponentsControl()));
    }
}
