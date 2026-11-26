package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentPower;

@CardRegistration(set = "MKM", collectorNumber = "140")
@CardRegistration(set = "MKM", collectorNumber = "407")
public class PyrotechnicPerformer extends Card {

    public PyrotechnicPerformer() {
        addMorph("{R}");
        addTrigger(Triggers.turnedFaceUp(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                DealDamageEffect.to(Subjects.eachOpponent(), new TriggeringPermanentPower())
                        .byTriggeringPermanent());
    }
}
