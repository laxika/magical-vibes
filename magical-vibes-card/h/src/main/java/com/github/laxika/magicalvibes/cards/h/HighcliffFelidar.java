package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesGreatestPowerCreatureToDestroyEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GN2", collectorNumber = "1")
public class HighcliffFelidar extends Card {

    public HighcliffFelidar() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new EachOpponentChoosesGreatestPowerCreatureToDestroyEffect());
    }
}
