package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMaySacrificeLandPutSourceOnTopEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "121")
public class ShivanWumpus extends Card {

    public ShivanWumpus() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new AnyPlayerMaySacrificeLandPutSourceOnTopEffect());
    }
}
