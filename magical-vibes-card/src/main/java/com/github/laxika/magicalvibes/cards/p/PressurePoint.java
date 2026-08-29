package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FRF", collectorNumber = "21")
public class PressurePoint extends Card {

    public PressurePoint() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new TapPermanentsEffect(TapUntapScope.TARGET));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
