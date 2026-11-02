package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EndureEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "141")
public class DusyutEarthcarver extends Card {

    public DusyutEarthcarver() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new EndureEffect(3));
    }
}
