package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTJ", collectorNumber = "246")
public class OasisGardener extends Card {

    public OasisGardener() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(2));
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
