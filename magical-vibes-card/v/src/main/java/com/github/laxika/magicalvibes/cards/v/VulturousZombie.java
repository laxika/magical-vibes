package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "238")
@CardRegistration(set = "CMD", collectorNumber = "236")
@CardRegistration(set = "C15", collectorNumber = "238")
@CardRegistration(set = "C16", collectorNumber = "228")
@CardRegistration(set = "CMA", collectorNumber = "196")
@CardRegistration(set = "CM2", collectorNumber = "170")
public class VulturousZombie extends Card {

    public VulturousZombie() {
        addTrigger(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN, ObjectRelation.OPPONENT_OWNS),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
