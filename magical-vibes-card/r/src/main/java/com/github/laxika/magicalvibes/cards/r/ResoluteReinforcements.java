package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "145")
@CardRegistration(set = "DMU", collectorNumber = "29")
public class ResoluteReinforcements extends Card {

    public ResoluteReinforcements() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.whiteSoldier(1));
    }
}
