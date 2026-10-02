package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetSpellOrNonlandPermanentToHandAndPerpetuallyIncreaseCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "YSNC", collectorNumber = "6")
public class NightclubBouncer extends Card {

    public NightclubBouncer() {
        target(TargetFilters.nonlandPermanentAnOpponentControls())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ReturnTargetSpellOrNonlandPermanentToHandAndPerpetuallyIncreaseCastCostEffect());
    }
}
