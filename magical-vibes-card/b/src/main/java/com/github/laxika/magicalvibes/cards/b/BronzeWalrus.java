package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HBG", collectorNumber = "254")
public class BronzeWalrus extends Card {

    public BronzeWalrus() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ScryEffect(2));
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
