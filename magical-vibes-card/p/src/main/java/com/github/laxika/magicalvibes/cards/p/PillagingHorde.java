package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "POR", collectorNumber = "142")
@CardRegistration(set = "VMA", collectorNumber = "180")
public class PillagingHorde extends Card {

    public PillagingHorde() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SacrificeUnlessDiscardCardTypeEffect(null, true));
    }
}
