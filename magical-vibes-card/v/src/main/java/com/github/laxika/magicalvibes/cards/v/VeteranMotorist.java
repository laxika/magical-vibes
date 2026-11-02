package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KLD", collectorNumber = "188")
@CardRegistration(set = "KLR", collectorNumber = "212")
public class VeteranMotorist extends Card {

    public VeteranMotorist() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ScryEffect(2));
        addEffect(EffectSlot.ON_CREWS_VEHICLE,
                new BoostReferencedPermanentEffect(PermanentReference.TRIGGERING, 1, 1));
    }
}
