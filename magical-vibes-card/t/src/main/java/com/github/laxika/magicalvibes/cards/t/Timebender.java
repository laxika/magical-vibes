package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AdjustTimeCountersOnTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "50")
@CardRegistration(set = "TSR", collectorNumber = "93")
public class Timebender extends Card {

    public Timebender() {
        addMorph("{U}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS), new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Remove two time counters",
                        new AdjustTimeCountersOnTargetEffect(false)),
                new ChooseOneEffect.ChooseOneOption("Put two time counters",
                        new AdjustTimeCountersOnTargetEffect(true)))));
    }
}
