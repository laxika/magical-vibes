package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TOR", collectorNumber = "18")
@CardRegistration(set = "VMA", collectorNumber = "52")
public class TerohsFaithful extends Card {

    public TerohsFaithful() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(4));
    }
}
