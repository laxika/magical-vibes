package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OGW", collectorNumber = "115")
@CardRegistration(set = "PIO", collectorNumber = "297")
public class PyromancersAssault extends Card {

    public PyromancersAssault() {
        addTrigger(Triggers.youCastSpell().nthThisTurn(2), new DealDamageToAnyTargetEffect(2));
    }
}
