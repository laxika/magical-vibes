package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutAuraFromHandOntoSelfEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "63")
@CardRegistration(set = "USG", collectorNumber = "58")
public class AcademyResearchers extends Card {

    public AcademyResearchers() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(new PutAuraFromHandOntoSelfEffect(), "Put an Aura from your hand onto the battlefield?"));
    }
}
