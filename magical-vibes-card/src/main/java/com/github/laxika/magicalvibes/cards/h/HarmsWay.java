package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "14")
public class HarmsWay extends Card {

    public HarmsWay() {
        addEffect(EffectSlot.SPELL, new PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect(2));
    }
}
