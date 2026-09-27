package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSC", collectorNumber = "572")
public class WarMachineJamesRhodes extends Card {

    public WarMachineJamesRhodes() {
        // Whenever War Machine attacks, tap up to one target creature.
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.ON_ATTACK, new TapPermanentsEffect(TapUntapScope.TARGET));
    }
}
