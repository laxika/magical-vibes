package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "DOM", collectorNumber = "6")
@CardRegistration(set = "GNT", collectorNumber = "8")
public class BenalishMarshal extends Card {

    public BenalishMarshal() {
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.otherCreaturesYouControl()));
    }
}
