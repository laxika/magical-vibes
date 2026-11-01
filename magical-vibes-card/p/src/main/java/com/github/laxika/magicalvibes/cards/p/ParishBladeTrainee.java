package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MoveDyingSourceCountersToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "29")
@CardRegistration(set = "DBL", collectorNumber = "296")
public class ParishBladeTrainee extends Card {

    public ParishBladeTrainee() {
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                MoveDyingSourceCountersToTargetCreatureEffect.alwaysTriggers(true));
    }
}
