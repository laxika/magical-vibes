package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCounterTypeOnControlledPermanentThenPutOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "NCC", collectorNumber = "22")
@CardRegistration(set = "NCC", collectorNumber = "123")
public class AvenCourier extends Card {

    public AvenCourier() {
        target(TargetFilters.permanentYouControl()).addEffect(
                EffectSlot.ON_ATTACK,
                new ChooseCounterTypeOnControlledPermanentThenPutOnTargetPermanentEffect());
    }
}
