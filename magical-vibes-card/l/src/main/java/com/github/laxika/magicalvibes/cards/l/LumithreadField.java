package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "FUT", collectorNumber = "25")
public class LumithreadField extends Card {

    public LumithreadField() {
        addStaticEffect(new StaticBoostEffect(0, 1, Subjects.creaturesYouControl()));
        addMorph("{1}{W}");
    }
}
