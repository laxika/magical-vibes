package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventXDamageToControllerAndPermanentsAndRedirectToAnyTargetEffect;

@CardRegistration(set = "AVR", collectorNumber = "18")
public class DivineDeflection extends Card {

    public DivineDeflection() {
        addEffect(EffectSlot.SPELL, new PreventXDamageToControllerAndPermanentsAndRedirectToAnyTargetEffect());
    }
}
