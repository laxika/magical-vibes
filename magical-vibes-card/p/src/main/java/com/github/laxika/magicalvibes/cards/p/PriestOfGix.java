package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "150")
@CardRegistration(set = "DDE", collectorNumber = "7")
public class PriestOfGix extends Card {

    public PriestOfGix() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new AwardManaEffect(ManaColor.BLACK, 3));
    }
}
