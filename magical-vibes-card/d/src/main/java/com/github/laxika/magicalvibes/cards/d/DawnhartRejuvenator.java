package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "180")
@CardRegistration(set = "DBL", collectorNumber = "180")
public class DawnhartRejuvenator extends Card {

    public DawnhartRejuvenator() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(3));
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
