package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "55")
public class DjinniWindseer extends Card {

    public DjinniWindseer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new RollD20Effect(
                new ScryEffect(1),
                new ScryEffect(2),
                new ScryEffect(3)));
    }
}
