package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DTK", collectorNumber = "140")
@CardRegistration(set = "SLD", collectorNumber = "313")
@CardRegistration(set = "SLD", collectorNumber = "1999")
@CardRegistration(set = "WOT", collectorNumber = "44")
@CardRegistration(set = "MOC", collectorNumber = "285")
public class ImpactTremors extends Card {

    public ImpactTremors() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                DealDamageEffect.to(Subjects.eachOpponent(), 1));
    }
}
