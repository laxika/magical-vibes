package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayScryEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONE", collectorNumber = "235")
public class MyrCustodian extends Card {

    public MyrCustodian() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ScryEffect(2));
        addTrigger(Triggers.enters(ObjectRelation.THIS), EachPlayerMayScryEffect.forOpponents(1));
    }
}
