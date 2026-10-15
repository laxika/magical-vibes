package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesInsteadOfDyingWithLifeLossEffect;
import com.github.laxika.magicalvibes.model.effect.RedistributePlayerLifeTotalsEffect;

@CardRegistration(set = "WHO", collectorNumber = "580")
public class TheDoctorsTomb extends Card {

    public TheDoctorsTomb() {
        addEffect(EffectSlot.STATIC, new ExileCreaturesInsteadOfDyingWithLifeLossEffect(2));
        addEffect(EffectSlot.CHAOS_TRIGGERED, new RedistributePlayerLifeTotalsEffect());
    }
}
