package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "MKM", collectorNumber = "231")
public class ShadyInformant extends Card {

    public ShadyInformant() {
        addMorph("{2}{B/R}{B/R}");
        addEffect(EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(2));
    }
}
