package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THS", collectorNumber = "187")
public class AshenRider extends Card {

    public AshenRider() {
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileTargetPermanentEffect())
                .addEffect(EffectSlot.ON_DEATH, new ExileTargetPermanentEffect());
    }
}
