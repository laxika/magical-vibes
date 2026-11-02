package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PIO", collectorNumber = "81")
@CardRegistration(set = "MOM", collectorNumber = "328")
public class ZephyrWinder extends Card {

    public ZephyrWinder() {
        target(TargetFilters.creature(), 0, 1)
                .addTrigger(Triggers.dealsCombatDamageToPlayer(),
                        new UntapPermanentsEffect(TapUntapScope.TARGET));
    }
}
