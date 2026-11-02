package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEqualToSpellManaValueToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "242")
@CardRegistration(set = "2X2", collectorNumber = "236")
@CardRegistration(set = "TSR", collectorNumber = "257")
@CardRegistration(set = "DSC", collectorNumber = "222")
@CardRegistration(set = "SCD", collectorNumber = "233")
@CardRegistration(set = "ARC", collectorNumber = "88")
public class KaervekTheMerciless extends Card {

    public KaervekTheMerciless() {
        addTrigger(Triggers.opponentCastsSpell(),
                new DealDamageEqualToSpellManaValueToAnyTargetEffect(null));
    }
}
