package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AttachAurasToSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M15", collectorNumber = "5")
public class BoonweaverGiant extends Card {

    public BoonweaverGiant() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), AttachAurasToSourceEffect.oneAuraSearch());
    }
}
