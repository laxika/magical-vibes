package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSubjectEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EMN", collectorNumber = "165")
@CardRegistration(set = "SIR", collectorNumber = "209")
public class PermeatingMass extends Card {

    public PermeatingMass() {
        addTrigger(Triggers.dealsCombatDamageToCreature(), BecomeCopyOfSubjectEffect.of(Subjects.source()).by(Subjects.triggering()));
    }
}
