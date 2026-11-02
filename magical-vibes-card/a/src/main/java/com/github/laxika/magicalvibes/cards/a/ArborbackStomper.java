package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KLD", collectorNumber = "142")
@CardRegistration(set = "KLR", collectorNumber = "152")
public class ArborbackStomper extends Card {

    public ArborbackStomper() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(5));
    }
}
