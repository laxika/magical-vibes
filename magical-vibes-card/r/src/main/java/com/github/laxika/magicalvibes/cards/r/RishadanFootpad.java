package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesPermanentUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "94")
public class RishadanFootpad extends Card {

    public RishadanFootpad() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new EachOpponentSacrificesPermanentUnlessPaysEffect("{2}"));
    }
}
