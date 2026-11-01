package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "193")
@CardRegistration(set = "WTH", collectorNumber = "91")
@CardRegistration(set = "HOP", collectorNumber = "48")
public class BogardanFirefiend extends Card {

    public BogardanFirefiend() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DealDamageToTargetCreatureEffect(2));
    }
}
