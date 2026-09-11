package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdjustEachCounterKindOnTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "NEO", collectorNumber = "244")
public class DramatistsPuppet extends Card {

    public DramatistsPuppet() {
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AdjustEachCounterKindOnTargetEffect());
    }
}
