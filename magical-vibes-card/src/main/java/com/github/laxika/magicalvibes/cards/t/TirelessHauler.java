package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.d.DireStrainBrawler;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "MID", collectorNumber = "203")
public class TirelessHauler extends Card {

    public TirelessHauler() {
        setBackFaceCard(new DireStrainBrawler());
    }

    @Override
    public String getBackFaceClassName() {
        return "DireStrainBrawler";
    }
}
