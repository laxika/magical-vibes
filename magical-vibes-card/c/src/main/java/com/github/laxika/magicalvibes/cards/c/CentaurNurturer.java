package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WAR", collectorNumber = "156")
public class CentaurNurturer extends Card {

    public CentaurNurturer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(3));
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
