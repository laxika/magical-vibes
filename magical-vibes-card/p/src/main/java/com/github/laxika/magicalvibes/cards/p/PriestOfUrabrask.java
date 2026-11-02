package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NPH", collectorNumber = "90")
public class PriestOfUrabrask extends Card {

    public PriestOfUrabrask() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new AwardManaEffect(ManaColor.RED, 3));
    }
}
