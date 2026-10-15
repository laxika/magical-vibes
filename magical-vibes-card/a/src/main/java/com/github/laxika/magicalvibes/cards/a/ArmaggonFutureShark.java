package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TMT", collectorNumber = "58")
@CardRegistration(set = "TMT", collectorNumber = "264")
public class ArmaggonFutureShark extends Card {

    public ArmaggonFutureShark() {
        target(TargetFilters.creature(), 0, 3)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DestroyEachTargetPermanentEffect());
    }
}
