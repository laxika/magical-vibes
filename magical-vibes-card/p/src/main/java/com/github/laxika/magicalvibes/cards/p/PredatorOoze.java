package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DKA", collectorNumber = "124")
@CardRegistration(set = "C20", collectorNumber = "185")
public class PredatorOoze extends Card {

    public PredatorOoze() {
        addEffect(EffectSlot.ON_ATTACK, new PutCountersOnSourceEffect(1, 1, 1));
        addTrigger(Triggers.creatureDies(ObjectRelation.DAMAGED_BY_THIS), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
