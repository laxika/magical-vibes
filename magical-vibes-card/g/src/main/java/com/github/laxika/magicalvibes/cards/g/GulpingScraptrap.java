package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONE", collectorNumber = "96")
public class GulpingScraptrap extends Card {

    public GulpingScraptrap() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ProliferateEffect());
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ProliferateEffect());
    }
}
