package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "MID", collectorNumber = "120")
@CardRegistration(set = "DBL", collectorNumber = "120")
public class ShadyTraveler extends Card {

    public ShadyTraveler() {
        setBackFaceCard(new StalkingPredator());
    }

    @Override
    public String getBackFaceClassName() {
        return "StalkingPredator";
    }
}
