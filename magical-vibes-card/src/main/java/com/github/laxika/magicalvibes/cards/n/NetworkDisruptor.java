package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "NEO", collectorNumber = "71")
public class NetworkDisruptor extends Card {

    public NetworkDisruptor() {
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TapPermanentsEffect(TapUntapScope.TARGET));
    }
}
