package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "196")
@CardRegistration(set = "MB1", collectorNumber = "196")
@CardRegistration(set = "SIR", collectorNumber = "188")
public class BywayCourier extends Card {

    public BywayCourier() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), CreateTokenEffect.ofClueToken(1));
    }
}
