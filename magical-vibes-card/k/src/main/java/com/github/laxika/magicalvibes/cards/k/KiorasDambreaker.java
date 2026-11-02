package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WAR", collectorNumber = "58")
@CardRegistration(set = "RVR", collectorNumber = "48")
public class KiorasDambreaker extends Card {

    public KiorasDambreaker() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ProliferateEffect());
    }
}
