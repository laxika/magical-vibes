package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ZNR", collectorNumber = "25")
public class MakindiOx extends Card {

    public MakindiOx() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                        new TapPermanentsEffect(TapUntapScope.TARGET));
    }
}
