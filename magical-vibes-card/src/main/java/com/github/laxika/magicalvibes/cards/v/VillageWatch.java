package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "MID", collectorNumber = "165")
public class VillageWatch extends Card {

    public VillageWatch() {
        setBackFaceCard(new VillageReavers());
    }

    @Override
    public String getBackFaceClassName() {
        return "VillageReavers";
    }
}
