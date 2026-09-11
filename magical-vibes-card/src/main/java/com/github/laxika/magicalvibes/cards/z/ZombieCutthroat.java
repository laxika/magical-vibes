package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LifeCastingCost;

@CardRegistration(set = "SCG", collectorNumber = "81")
public class ZombieCutthroat extends Card {

    public ZombieCutthroat() {
        addMorph("{0}", new LifeCastingCost(5));
    }
}
