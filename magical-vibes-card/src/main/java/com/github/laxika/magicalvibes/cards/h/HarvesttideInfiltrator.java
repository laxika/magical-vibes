package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "MID", collectorNumber = "143")
public class HarvesttideInfiltrator extends Card {

    public HarvesttideInfiltrator() {
        setBackFaceCard(new HarvesttideAssailant());
    }

    @Override
    public String getBackFaceClassName() {
        return "HarvesttideAssailant";
    }
}
