package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ULG", collectorNumber = "3")
public class BurstOfEnergy extends Card {

    public BurstOfEnergy() {
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(TapUntapScope.TARGET));
    }
}
