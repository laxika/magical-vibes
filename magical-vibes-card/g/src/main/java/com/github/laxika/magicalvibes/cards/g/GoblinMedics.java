package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ULG", collectorNumber = "79")
@CardRegistration(set = "DMR", collectorNumber = "124")
@CardRegistration(set = "MOC", collectorNumber = "283")
public class GoblinMedics extends Card {

    public GoblinMedics() {
        addTrigger(Triggers.becomesTapped(), new DealDamageToAnyTargetEffect(1));
    }
}
