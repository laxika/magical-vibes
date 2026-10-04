package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "ELD", collectorNumber = "128")
@CardRegistration(set = "ELD", collectorNumber = "363")
public class IrencragPyromancer extends Card {

    public IrencragPyromancer() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS_SECOND_CARD, new DealDamageToAnyTargetEffect(3));
    }
}
