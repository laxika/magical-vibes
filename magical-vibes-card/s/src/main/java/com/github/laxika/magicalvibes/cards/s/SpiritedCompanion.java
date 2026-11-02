package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "38")
@CardRegistration(set = "SLD", collectorNumber = "896")
@CardRegistration(set = "MOC", collectorNumber = "208")
@CardRegistration(set = "BLC", collectorNumber = "155")
public class SpiritedCompanion extends Card {

    public SpiritedCompanion() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect());
    }
}
