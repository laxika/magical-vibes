package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "TSP", collectorNumber = "256")
@CardRegistration(set = "TSR", collectorNumber = "268")
public class Hivestone extends Card {

    public Hivestone() {
        addStaticEffect(new GrantSubtypeEffect(CardSubtype.SLIVER, Subjects.creaturesYouControl()));
    }
}
