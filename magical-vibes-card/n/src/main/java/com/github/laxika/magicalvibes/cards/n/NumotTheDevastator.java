package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "160")
@CardRegistration(set = "CMD", collectorNumber = "215")
public class NumotTheDevastator extends Card {

    public NumotTheDevastator() {
        target(TargetFilters.land(), 0, 2)
                .addTrigger(Triggers.dealsCombatDamageToPlayer(),
                        new MayPayManaEffect("{2}{R}", new DestroyEachTargetPermanentEffect(),
                                "Pay {2}{R}?"));
    }
}
