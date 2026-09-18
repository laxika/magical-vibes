package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PIO", collectorNumber = "81")
public class ZephyrWinder extends Card {

    public ZephyrWinder() {
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                        new UntapPermanentsEffect(TapUntapScope.TARGET));
    }
}
