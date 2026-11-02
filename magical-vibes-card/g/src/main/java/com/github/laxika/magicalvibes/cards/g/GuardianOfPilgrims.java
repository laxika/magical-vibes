package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "26")
@CardRegistration(set = "EMN", collectorNumber = "30")
@CardRegistration(set = "SIR", collectorNumber = "32")
public class GuardianOfPilgrims extends Card {

    public GuardianOfPilgrims() {
        // When this creature enters, target creature gets +1/+1 until end of turn.
        target(TargetFilters.creature()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new BoostTargetCreatureEffect(1, 1));
    }
}
