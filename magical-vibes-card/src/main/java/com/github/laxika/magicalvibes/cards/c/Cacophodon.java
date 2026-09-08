package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RIX", collectorNumber = "123")
public class Cacophodon extends Card {

    public Cacophodon() {
        // Enrage — Whenever this creature is dealt damage, untap target permanent.
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.ON_DEALT_DAMAGE, new UntapPermanentsEffect(TapUntapScope.TARGET));
    }
}
