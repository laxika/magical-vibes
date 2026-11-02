package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M10", collectorNumber = "158")
@CardRegistration(set = "RAV", collectorNumber = "144")
@CardRegistration(set = "DDN", collectorNumber = "48")
public class SparkmageApprentice extends Card {

    public SparkmageApprentice() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DealDamageToAnyTargetEffect(1));
    }
}
