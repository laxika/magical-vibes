package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesPermanentUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "93")
public class RishadanCutpurse extends Card {

    public RishadanCutpurse() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new EachOpponentSacrificesPermanentUnlessPaysEffect("{1}"));
    }
}
