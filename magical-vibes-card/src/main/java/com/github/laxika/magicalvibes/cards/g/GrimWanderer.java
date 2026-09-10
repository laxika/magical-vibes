package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Morbid;

@CardRegistration(set = "AFR", collectorNumber = "107")
public class GrimWanderer extends Card {

    public GrimWanderer() {
        setCastCondition(new Morbid());
    }
}
