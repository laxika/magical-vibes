package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ROE", collectorNumber = "77")
public class MerfolkSkyscout extends Card {

    public MerfolkSkyscout() {
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.ON_ATTACK, new UntapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.ON_BLOCK, new UntapPermanentsEffect(TapUntapScope.TARGET));
    }
}
