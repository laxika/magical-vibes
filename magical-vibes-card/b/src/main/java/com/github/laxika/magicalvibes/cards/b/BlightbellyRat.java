package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONE", collectorNumber = "85")
public class BlightbellyRat extends Card {

    public BlightbellyRat() {
        addStaticEffect(new ToxicEffect(1));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ProliferateEffect());
    }
}
