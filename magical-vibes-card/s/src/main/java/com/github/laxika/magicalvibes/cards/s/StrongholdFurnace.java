package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OHOP", collectorNumber = "37")
@CardRegistration(set = "OPCA", collectorNumber = "75")
@CardRegistration(set = "MOC", collectorNumber = "160")
public class StrongholdFurnace extends Card {

    public StrongholdFurnace() {
        addStaticEffect(new DoubleDamageEffect());
        addTrigger(Triggers.chaosEnsues(), new DealDamageToAnyTargetEffect(1));
    }
}
