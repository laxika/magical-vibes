package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CHK", collectorNumber = "47")
public class TerashisCry extends Card {

    public TerashisCry() {
        target(TargetFilters.creature(), 0, 3)
                .addEffect(EffectSlot.SPELL, new TapPermanentsEffect(TapUntapScope.TARGET));
    }
}
